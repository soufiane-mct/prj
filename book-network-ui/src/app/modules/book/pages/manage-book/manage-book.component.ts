import { Component, OnInit } from '@angular/core';
import { BookRequest } from '../../../../services/models/book-request';
import { BookService } from '../../../../services/services/book.service';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CategoryService, Category } from '../../../../services/services/category.service';
import { Location as NgLocation } from '@angular/common';
import { SharedModule } from '../../../../shared/shared.module';
import { LocationService, LocationData } from '../../../../shared/services/location.service';

@Component({
  selector: 'app-manage-book',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, SharedModule],
  templateUrl: './manage-book.component.html',
  styleUrls: ['./manage-book.component.scss']
})
export class ManageBookComponent implements OnInit {

  errorMsg: Array<string> = [];
  private errorTimeout: any;
  showLocationModal = false;
  showRetryButton = false;
  locationSearch = '';
  bookRequest: BookRequest = {
    authorName: '',
    location: '',
    synopsis: '',
    title: '',
    latitude: undefined,
    longitude: undefined,
    fullAddress: ''
  };
  selectedBookCover: any;
  selectedPicture: string | undefined;
  categories: Category[] = [];

  // Format location for display
  formatLocation(): string {
    const lat = this.bookRequest?.latitude;
    const lng = this.bookRequest?.longitude;
    
    if (lat != null && lng != null && !isNaN(lat) && !isNaN(lng)) {
      return `Lat: ${Number(lat).toFixed(4)}, Lng: ${Number(lng).toFixed(4)}`;
    }
    return this.bookRequest?.location || 'No location selected';
  }

  // Center map on user's current location
  async centerOnMe(): Promise<void> {
    if (!navigator.geolocation) {
      this.errorMsg = ['Geolocation is not supported by your browser.'];
      return;
    }

    // Clear previous errors and timeout
    this.clearError();

    // Check if we have permission to access geolocation
    const permissionStatus = await navigator.permissions?.query({ name: 'geolocation' as PermissionName }).catch(() => null);
    
    // If permission is explicitly denied, show a more helpful message
    if (permissionStatus?.state === 'denied') {
      this.showError('Location access is not allowed in your browser. Please enable location permissions and try again, or enter your location manually.');
      this.showRetryButton = true;
    }

    // Request location with options for better accuracy and timeout
    const options = {
      enableHighAccuracy: true,
      timeout: 10000, // 10 seconds
      maximumAge: 0 // Force fresh location
    };

    return new Promise((resolve) => {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          if (this.bookRequest) {
            this.bookRequest.latitude = position.coords.latitude;
            this.bookRequest.longitude = position.coords.longitude;
            // Optionally trigger a reverse geocode to get address
            this.updateLocationInfo(position.coords.latitude, position.coords.longitude);
            resolve();
          }
        },
        (error) => {
          console.error('Error getting location:', error);
          let errorMessage = 'Unable to retrieve your location.';
          let instructions: string[] = [];
          
          switch(error.code) {
            case error.PERMISSION_DENIED:
              errorMessage = 'Location access is not allowed in your browser. Please enable location permissions and try again, or enter your location manually.';
              this.showRetryButton = true;
              this.showError(errorMessage);
              break;
            case error.POSITION_UNAVAILABLE:
              errorMessage = 'Location information is unavailable.';
              instructions = ['Please check your internet connection and try again.'];
              break;
            case error.TIMEOUT:
              errorMessage = 'The request to get your location timed out.';
              instructions = ['Please try again in a moment.'];
              break;
            default:
              errorMessage = 'An unknown error occurred while trying to get your location.';
          }
          
          this.errorMsg = [errorMessage, ...instructions];
          resolve();
        },
        options
      );
    });
  }

  // Search for a location
  searchLocation(): void {
    if (!this.locationSearch.trim()) return;
    
    // This is a simplified example - you might want to use a geocoding service
    // like Google Maps Geocoding API or similar
    console.log('Searching for location:', this.locationSearch);
    
    // For now, we'll just show a message
    this.errorMsg = ['Location search requires integration with a geocoding service.'];
  }

  // Get city name from coordinates using reverse geocoding
  private async getCityName(lat: number, lng: number): Promise<string | null> {
    try {
      const response = await fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}&addressdetails=1`);
      const data = await response.json();
      
      // Try to get the most specific location name available
      if (data.address) {
        return (
          data.address.city || 
          data.address.town || 
          data.address.village || 
          data.address.municipality ||
          data.address.county ||
          data.address.state ||
          data.display_name?.split(',')[0] ||
          null
        );
      }
    } catch (error) {
      console.error('Error getting city name:', error);
    }
    return null;
  }

  // Update location information using coordinates with reverse geocoding
  private updateLocationInfo(lat: number, lng: number): void {
    // First set the coordinates
    this.bookRequest.latitude = lat;
    this.bookRequest.longitude = lng;
    
    // Default display with coordinates
    const coords = `(${lat.toFixed(4)}, ${lng.toFixed(4)})`;
    this.bookRequest.location = coords;
    
    // Try to get city name using reverse geocoding
    this.getCityName(lat, lng).then(cityName => {
      if (cityName) {
        // If we got a city name, use it in the full address
        this.bookRequest.fullAddress = cityName;
        this.bookRequest.location = cityName; // Also update the location field
      } else {
        // Fallback to coordinates if we can't get the city name
        this.bookRequest.fullAddress = `Location ${coords}`;
      }
      
      // Update the location service with the final values
      this.locationService.updateLocation({
        latitude: lat,
        longitude: lng,
        fullAddress: this.bookRequest.fullAddress,
        address: this.bookRequest.location,
        locationName: this.bookRequest.fullAddress
      });
    }).catch(() => {
      // On error, just use the coordinates
      this.bookRequest.fullAddress = `Location ${coords}`;
      this.locationService.updateLocation({
        latitude: lat,
        longitude: lng,
        fullAddress: this.bookRequest.fullAddress,
        address: coords,
        locationName: coords
      });
    });
  }

  // Location modal methods
  openLocationModal(): void {
    this.showLocationModal = true;
    // Wait for the modal to be rendered in the DOM
    setTimeout(() => {
      const mapPicker = document.querySelector('app-map-picker') as any;
      if (mapPicker && mapPicker.initMapIfNeeded) {
        mapPicker.initMapIfNeeded();
      }
    }, 100);
  }

  closeLocationModal(): void {
    this.showLocationModal = false;
  }

  onRetryLocation(): void {
    this.centerOnMe();
  }

  private showError(message: string): void {
    this.clearError();
    this.errorMsg = [message];
    this.errorTimeout = setTimeout(() => {
      this.errorMsg = [];
    }, 5000);
  }

  private clearError(): void {
    if (this.errorTimeout) {
      clearTimeout(this.errorTimeout);
      this.errorTimeout = null;
    }
    this.errorMsg = [];
  }

  clearLocation(): void {
    this.bookRequest.latitude = undefined;
    this.bookRequest.longitude = undefined;
    this.bookRequest.location = '';
    this.bookRequest.fullAddress = '';
    this.locationService.updateLocation(null);
  }

  locationDisplay(): string {
    return this.locationService.formatLocation({
      latitude: this.bookRequest.latitude,
      longitude: this.bookRequest.longitude,
      fullAddress: this.bookRequest.fullAddress,
      address: this.bookRequest.location,
      locationName: this.bookRequest.location
    });
  }

  onLocationSelected(location: LocationData | any): void {
    const lat = location?.lat ?? location?.latitude;
    const lng = location?.lng ?? location?.longitude;
    
    if (lat !== undefined && lng !== undefined) {
      this.bookRequest.latitude = lat;
      this.bookRequest.longitude = lng;
      
      // Use the provided address if available, otherwise use coordinates
      if (location?.address || location?.locationName || location?.display_name) {
        this.bookRequest.location = location.locationName || location.address || location.display_name || '';
        this.bookRequest.fullAddress = location.address || location.locationName || location.display_name || '';
      } else {
        this.updateLocationInfo(lat, lng);
        return; // updateLocationInfo already updates the location service
      }
      
      // Update the location service
      this.locationService.updateLocation({
        latitude: lat,
        longitude: lng,
        fullAddress: this.bookRequest.fullAddress,
        address: this.bookRequest.location,
        locationName: this.bookRequest.location
      });
    }
  }


  constructor(
    private bookService: BookService,
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private categoryService: CategoryService,
    private locationService: LocationService
  ) {
  }

  ngOnInit(): void {
    this.categoryService.getAllCategories().subscribe(categories => {
      this.categories = categories;
    });
    const bookId = this.activatedRoute.snapshot.params['bookId'];
    if (bookId) {
      this.bookService.findBookById({
        'book-id': bookId
      }).subscribe({
        next: (book) => {
          this.bookRequest = {
            id: book.id,
            title: book.title || '',
            authorName: book.authorName || '',
            synopsis: book.synopsis || '',
            location: book.location || '',
            latitude: book.latitude,
            longitude: book.longitude,
            fullAddress: book.fullAddress || book.location || '',
            shareable: book.shareable,
            categoryId: book.categoryId
          };
          
          // If we have coordinates but no location text, set a default location text
          if (book.latitude != null && book.longitude != null && !book.location) {
            const lat = Number(book.latitude);
            const lng = Number(book.longitude);
            if (!isNaN(lat) && !isNaN(lng)) {
              this.bookRequest.location = `Lat: ${lat.toFixed(4)}, Lng: ${lng.toFixed(4)}`;
              this.bookRequest.fullAddress = this.bookRequest.location;
            }
          }
          
          // Set the book cover if it exists
          if (book.cover) {
            this.selectedPicture = 'data:image/jpg;base64,' + book.cover;
          }
        }
      });
    }
  }

  saveBook(): void {
    if (!this.bookRequest) {
      this.errorMsg = ['Invalid book data'];
      return;
    }

    this.bookService.saveBook({
      body: this.bookRequest
    }).subscribe({
      next: (bookId: number) => {
        if (this.selectedBookCover) {
          this.bookService.uploadBookCoverPictureRaw(bookId, this.selectedBookCover)
            .subscribe({
              next: () => {
                this.router.navigate(['/products/my-products']);
              },
              error: (err: any) => {
                this.errorMsg = ['Cover upload failed.'];
                this.router.navigate(['/products/my-products']);
              }
            });
        } else {
          this.router.navigate(['/products/my-products']);
        }
      },
      error: (err: any) => {
        this.errorMsg = err.error?.validationErrors || ['Failed to save book'];
      }
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) {
      return;
    }
    
    this.selectedBookCover = input.files[0];

    if (this.selectedBookCover) {
      const reader = new FileReader();
      reader.onload = () => {
        this.selectedPicture = reader.result as string;
      };
      reader.readAsDataURL(this.selectedBookCover);
    }
  }


}
