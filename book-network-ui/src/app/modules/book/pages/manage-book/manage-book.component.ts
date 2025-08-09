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
import { Observable, of } from 'rxjs';

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
  selectedBookCovers: File[] = [];
  selectedVideo: File | null = null;
  // Store both previews and uploaded image URLs
  imagePreviews: {preview: string, url: string | null, type: 'image' | 'video'}[] = [];
  videoPreview: {preview: string, url: string | null, file: File | null} = { preview: '', url: null, file: null } as const;
  categories: Category[] = [];
  maxImages = 5;
  maxVideoSizeMB = 100; // 100MB max video size

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
            this.imagePreviews = [{
              preview: 'data:image/jpg;base64,' + book.cover,
              url: null,
              type: 'image' as const
            }];
          }
        }
      });
    }
  }

  // Add this property to track loading state
  isLoading = false;

  saveBook(): void {
    this.errorMsg = [];
    this.isLoading = true;
    
    console.log('Starting saveBook...');
    console.log('Selected video:', this.selectedVideo);
    console.log('Selected book covers:', this.selectedBookCovers);
    
    // Check if we have a location
    if ((!this.bookRequest.latitude || !this.bookRequest.longitude) && !this.bookRequest.location) {
      console.log('No location provided');
      this.errorMsg = ['Please select a location for your book.'];
      this.isLoading = false;
      return;
    }
    
    // If we have coordinates but no address, try to get the address
    if ((this.bookRequest.latitude && this.bookRequest.longitude) && !this.bookRequest.location) {
      console.log('Getting address from coordinates');
      this.updateLocationInfo(this.bookRequest.latitude, this.bookRequest.longitude);
    }
    
    console.log('Saving book with data:', this.bookRequest);
    
    // Save the book first
    const saveObservable = this.bookRequest.id 
      ? this.bookService.saveBook({
          body: {
            ...this.bookRequest
          }
        })
      : this.bookService.saveBook({
          body: this.bookRequest
        });
    
    saveObservable.subscribe({
      next: (book) => {
        console.log('Book saved successfully:', book);
        // Get the book ID from the response
        const bookId = typeof book === 'number' ? book : (book as any).id;
        console.log('Book ID:', bookId);
        
        console.log('Checking if we need to upload files...');
        console.log('Selected book covers length:', this.selectedBookCovers.length);
        console.log('Selected video exists:', !!this.selectedVideo);
        
        // If we have images or video to upload, do that now
        if (this.selectedBookCovers.length > 0 || this.selectedVideo) {
          console.log('Starting file uploads...');
          this.uploadBookImages(bookId);
        } else {
          console.log('No files to upload, navigating away');
          // No files to upload, just navigate
          this.isLoading = false;
          this.router.navigate(['/products/my-products']);
        }
      },
      error: (err: any) => {
        this.handleSaveError(err);
      }
    });
  }

  private uploadBookImages(bookId: number): void {
    console.log('uploadBookImages called with bookId:', bookId);
    console.log('selectedBookCovers:', this.selectedBookCovers);
    console.log('selectedVideo:', this.selectedVideo);
    
    if (!this.selectedBookCovers.length && !this.selectedVideo) {
      console.log('No files to upload, navigating away');
      this.isLoading = false;
      this.router.navigate(['/products/my-products']);
      return;
    }

    // First upload the video if present
    const uploadObservable = this.selectedVideo 
      ? this.uploadVideo(bookId, this.selectedVideo)
      : new Observable<{videoUrl: string | null}>(subscriber => {
          subscriber.next({ videoUrl: null });
          subscriber.complete();
        });

    uploadObservable.subscribe({
      next: (videoResponse: { videoUrl: string | null }) => {
        console.log('Video upload response received:', videoResponse);
        
        // Update video preview URL if video was uploaded
        if (videoResponse?.videoUrl) {
          console.log('Video upload completed, URL:', videoResponse.videoUrl);
          this.videoPreview.url = videoResponse.videoUrl;
        }

        // Then upload the images if present
        if (this.selectedBookCovers.length > 0) {
          console.log('Starting image upload for', this.selectedBookCovers.length, 'images');
          this.bookService.uploadBookImages(
            bookId,
            this.selectedBookCovers,
            true // Set first image as cover
          ).subscribe({
            next: (event: any) => {
              // Handle upload progress
              if (event.type === 'progress') {
                console.log(`Upload progress: ${event.percent}%`);
              } else if (event.type === 'complete' || event.body) {
                // Upload completed successfully
                const response = event.body || event.data;
                console.log('Image upload completed:', response);
                
                // Update the URLs in imagePreviews with the actual backend URLs
                if (response?.images?.length) {
                  response.images.forEach((img: any, index: number) => {
                    if (this.imagePreviews[index]) {
                      this.imagePreviews[index].url = img.imageUrl;
                    }
                  });
                }
                
                this.completeUploadProcess();
              }
            },
            error: (error: any) => {
              console.error('Error uploading images, but continuing with navigation', error);
              this.completeUploadProcess();
            }
          });
        } else {
          // No images to upload, just complete the process
          this.completeUploadProcess();
        }
      },
      error: (error: any) => {
        console.error('Error in upload process:', error);
        if (this.selectedBookCovers.length > 0) {
          // If there are images, continue with image upload even if video failed
          console.log('Video upload failed, but continuing with image upload');
          this.uploadImages(bookId);
        } else {
          this.completeUploadProcess();
        }
      }
    });
  }

  private uploadVideo(bookId: number, videoFile: File): Observable<{videoUrl: string}> {
    console.log('=== STARTING VIDEO UPLOAD ===');
    console.log('Book ID:', bookId);
    console.log('Video file name:', videoFile.name);
    console.log('Video file size:', videoFile.size, 'bytes');
    console.log('Video file type:', videoFile.type);
    
    const formData = new FormData();
    formData.append('file', videoFile);
    
    // Log FormData contents (for debugging)
    console.log('FormData contents:');
    for (let pair of (formData as any).entries()) {
      console.log(`- ${pair[0]}:`, pair[1]);
    }
    
    return new Observable(observer => {
      console.log('Creating upload observable...');
      console.log('Calling bookService.uploadBookVideo...');
      
      const subscription = this.bookService.uploadBookVideo(bookId, formData).subscribe({
        next: (event: any) => {
          console.log('=== VIDEO UPLOAD EVENT ===');
          console.log('Event type:', event.type);
          
          if (event.type === 'progress') {
            console.log(`Upload progress: ${event.percent}%`);
            return; // Don't complete on progress events
          }
          
          if (event.type === 'complete' && event.data) {
            console.log('=== VIDEO UPLOAD COMPLETE ===');
            console.log('Complete event data:', event.data);
            
            // The response from the server is in event.data
            const response = event.data;
            
            // The backend returns the video URL directly in the response
            const videoUrl = response.videoUrl || response.url || null;
            console.log('Extracted video URL:', videoUrl);
            
            if (!videoUrl) {
              console.warn('No video URL found in the response!');
              observer.error(new Error('No video URL in response'));
              return;
            }
            
            observer.next({ videoUrl });
            observer.complete();
          }
        },
        error: (error: any) => {
          console.error('=== VIDEO UPLOAD ERROR ===');
          console.error('Error object:', error);
          console.error('Error message:', error?.message);
          
          observer.error({
            message: 'Video upload failed',
            details: error?.message || 'Unknown error during upload',
            status: error?.status
          });
        },
        complete: () => {
          console.log('Video upload observable completed');
          // Don't complete the outer observable here, it will be completed in the next handler
        }
      });
      
      // Log subscription for debugging
      console.log('Upload subscription created:', subscription);
      
      // Return cleanup function
      return () => {
        console.log('Cleaning up video upload subscription');
        subscription.unsubscribe();
      };
    });
  }

  private uploadImages(bookId: number): void {
    if (this.selectedBookCovers.length === 0) {
      this.completeUploadProcess();
      return;
    }

    console.log('Starting image upload for', this.selectedBookCovers.length, 'images');
    this.bookService.uploadBookImages(
      bookId,
      this.selectedBookCovers,
      true // Set first image as cover
    ).subscribe({
      next: (event: any) => {
        // Handle upload progress
        if (event.type === 'progress') {
          console.log(`Upload progress: ${event.percent}%`);
        } else if (event.type === 'complete' || event.body) {
          // Upload completed successfully
          const response = event.body || event.data;
          console.log('Image upload completed:', response);
          
          // Update the URLs in imagePreviews with the actual backend URLs
          if (response?.images?.length) {
            response.images.forEach((img: any, index: number) => {
              if (this.imagePreviews[index]) {
                this.imagePreviews[index].url = img.imageUrl;
              }
            });
          }
          
          this.completeUploadProcess();
        }
      },
      error: (error: any) => {
        console.error('Error uploading images:', error);
        this.completeUploadProcess();
      }
    });
  }

  private completeUploadProcess(): void {
    console.log('Upload process completed, navigating to my products');
    this.isLoading = false;
    this.router.navigate(['/products/my-products']);
  }

  private handleUploadError(error: any): void {
    console.error('Error uploading files:', error);
    this.isLoading = false;
    
    // Extract error message from response if available
    let errorMessage = 'There was an issue uploading your files.';
    if (error.error?.message) {
      errorMessage = error.error.message;
    } else if (error.status === 0) {
      errorMessage = 'Unable to connect to server. Please check your connection.';
    } else if (error.status === 413) {
      errorMessage = 'The file(s) you are trying to upload are too large.';
    } else if (error.status === 415) {
      errorMessage = 'Unsupported file type. Please check the file formats.';
    }
    
    this.errorMsg = [errorMessage];
    // Still navigate to my-products even if upload fails
    setTimeout(() => {
      this.router.navigate(['/products/my-products']);
    }, 3000);
  }

  private handleSaveError(err: any): void {
    this.isLoading = false;
    
    // Handle different types of errors
    if (err.error) {
      if (err.error.validationErrors) {
        this.errorMsg = Array.isArray(err.error.validationErrors) 
          ? err.error.validationErrors 
          : [err.error.validationErrors];
      } else if (err.error.error) {
        this.errorMsg = [err.error.error];
      } else if (err.error.message) {
        this.errorMsg = [err.error.message];
      } else {
        this.errorMsg = ['Failed to save book. Please try again.'];
      }
    } else if (err.status === 0) {
      this.errorMsg = ['Unable to connect to server. Please check your internet connection.'];
    } else {
      this.errorMsg = ['An unexpected error occurred. Please try again.'];
    }
    
    console.error('Save book error:', err);
  }

  removeImage(index: number): void {
    this.imagePreviews.splice(index, 1);
    this.selectedBookCovers.splice(index, 1);
  }

  removeVideo(): void {
    this.videoPreview = { preview: '', url: null, file: null };
    this.selectedVideo = null;
  }

  setAsMain(index: number): void {
    if (index > 0 && index < this.imagePreviews.length) {
      // Move the selected image to the first position
      const [movedImage] = this.imagePreviews.splice(index, 1);
      this.imagePreviews.unshift(movedImage);
      
      // Update the selected files array to match
      if (this.selectedBookCovers.length > index) {
        const [movedFile] = this.selectedBookCovers.splice(index, 1);
        this.selectedBookCovers.unshift(movedFile);
      }
    }
  }

  /**
   * Handles file selection for book covers
   * @param event The file input change event
   */
  onFileSelected(event: Event, type: 'images' | 'video' = 'images'): void {
    const input = event.target as HTMLInputElement;
    
    if (!input.files || input.files.length === 0) return;
    
    if (type === 'video') {
      const file = input.files[0];
      
      // Check if file is a video
      if (!file.type.match('video.*')) {
        this.errorMsg = ['Only video files are allowed for video upload.'];
        return;
      }
      
      // Check video file size (max 50MB)
      if (file.size > this.maxVideoSizeMB * 1024 * 1024) {
        this.errorMsg = [`Video file is too large. Maximum size is ${this.maxVideoSizeMB}MB.`];
        return;
      }
      
      // Create video preview
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.videoPreview = {
          preview: e.target.result,
          url: null,
          file: file
        };
      };
      reader.readAsDataURL(file);
      
      // Store the video file
      this.selectedVideo = file;
      
    } else { // Handle images
      // Calculate how many more images we can add
      const remainingSlots = this.maxImages - this.imagePreviews.length;
      if (remainingSlots <= 0) {
        this.errorMsg = [`You can only upload up to ${this.maxImages} images.`];
        return;
      }
      
      // Convert FileList to array and take only the number of files we can add
      const files = Array.from(input.files).slice(0, remainingSlots);
      
      files.forEach(file => {
        // Check if file is an image
        if (!file.type.match('image.*')) {
          this.errorMsg = ['Only image files are allowed for image upload.'];
          return;
        }
        
        // Check file size (max 25MB)
        if (file.size > 25 * 1024 * 1024) {
          this.errorMsg = [`File ${file.name} is too large. Maximum size is 25MB.`];
          return;
        }
        
        // Create preview
        const reader = new FileReader();
        reader.onload = (e: any) => {
          this.imagePreviews.push({
            preview: e.target.result,
            url: null,
            type: 'image'
          });
        };
        reader.readAsDataURL(file);
        
        // Add to selected files
        this.selectedBookCovers.push(file);
      });
    }
    
    // Reset the file input
    input.value = '';
  }
}
