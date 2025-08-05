import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { PageResponseBookResponse } from '../../../../services/models/page-response-book-response';
import { BookService } from '../../../../services/services/book.service';
import { BookResponse } from '../../../../services/models/book-response';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { HttpTokenInterceptor } from '../../../../services/interceptor/http-token.interceptor';
import { BookCardComponent } from "../../components/book-card/book-card.component";
import { CategoryService, Category } from '../../../../services/services/category.service';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { GuestRentModalComponent } from '../../components/book-card/guest-rent-modal.component';
import { Location } from '@angular/common';
import { SharedModule } from '../../../../shared/shared.module';
import { MapPickerComponent } from '../../../../shared/components/map-picker/map-picker.component';

@Component({
  selector: 'app-book-list',
  standalone: true,
  imports: [
    CommonModule, 
    BookCardComponent, 
    FormsModule, 
    GuestRentModalComponent,
    SharedModule,
    MapPickerComponent
  ],
  templateUrl: './book-list.component.html',
  styleUrl: './book-list.component.scss',
})
export class BookListComponent implements OnInit {
  bookResponse: PageResponseBookResponse = { content: [] };
  page = 0;
  size = 12; // Increased default page size
  pages: number[] = [];
  message = '';
  level: 'success' | 'error' = 'success';
  Math = Math; // Make Math available in template
  categories: Category[] = [];
  selectedCategoryId: number | undefined;
  searchTerm: string = '';
  locationFilter: string = '';
  private _guestModalVisible = false;
  get guestModalVisible(): boolean {
    return this._guestModalVisible;
  }
  set guestModalVisible(value: boolean) {
    if (this._guestModalVisible !== value) {
      this._guestModalVisible = value;
      // Toggle body scroll and modal-open class when modal visibility changes
      if (value) {
        document.body.classList.add('modal-open');
      } else {
        document.body.classList.remove('modal-open');
      }
    }
  }
  guestModalBookTitle = '';
  guestModalBookId: number | undefined;
  isLoading = true;
  
  // Success notification properties
  showSuccessNotification = false;
  successMessage = '';
  
  // Map modal state
  private _showMapModal = false;
  get showMapModal(): boolean {
    return this._showMapModal;
  }
  set showMapModal(value: boolean) {
    if (this._showMapModal !== value) {
      this._showMapModal = value;
      // Toggle body scroll and modal-open class when modal visibility changes
      if (value) {
        document.body.classList.add('map-modal-open');
      } else {
        document.body.classList.remove('map-modal-open');
      }
    }
  }
  pendingLocation: { latitude: number; longitude: number; radius: number; locationName?: string } | null = null;

  selectedLocation: { latitude: number; longitude: number; radius: number; locationName?: string } | null = null;
  locationName: string = '';

  // Open the map picker modal
  openMapModal(): void {
    this.showMapModal = true;
    
    // Initialize the map after the modal is shown
    setTimeout(() => {
      const mapElement = document.querySelector('app-map-picker');
      if (mapElement) {
        // Trigger map initialization if needed
        const mapComponent = mapElement as any;
        if (mapComponent.initMapIfNeeded) {
          mapComponent.initMapIfNeeded();
        }
      }
    }, 100);
  }

  // Close the map picker modal
  closeMapModal(): void {
    this.showMapModal = false;
    // Reset pending location when closing without applying
    if (this.pendingLocation) {
      this.pendingLocation = null;
    }
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

  // Apply the selected location and close modal
  applyLocation(): void {
    if (this.selectedLocation) {
      // Use the city name if available, otherwise use coordinates
      const locationText = this.selectedLocation.locationName || 
                          `Near (${this.selectedLocation.latitude.toFixed(4)}, ${this.selectedLocation.longitude.toFixed(4)})`;
      this.locationFilter = locationText;
      this.page = 0; // Reset to first page when location changes
      this.onSearch(); // Trigger search with new location filter
    }
    this.showMapModal = false;
  }

  // Handle location selection from map picker
  onLocationSelected(location: { latitude: number; longitude: number; radius?: number } | null): void {
    if (location) {
      this.selectedLocation = {
        latitude: location.latitude,
        longitude: location.longitude,
        radius: location.radius || 5 // Default to 5km radius if not provided
      };
      // Store the pending location (in case user clicks apply)
      this.pendingLocation = { ...this.selectedLocation };
      
      // Get the city name for the selected location
      this.getCityName(location.latitude, location.longitude).then(cityName => {
        if (cityName) {
          this.locationName = cityName;
          if (this.selectedLocation) {
            this.selectedLocation.locationName = cityName;
          }
          if (this.pendingLocation) {
            this.pendingLocation = { ...this.pendingLocation, locationName: cityName };
          }
        } else {
          // Fallback to coordinates if city name can't be determined
          this.locationName = `${location.latitude.toFixed(4)}, ${location.longitude.toFixed(4)}`;
        }
      });
    } else {
      this.selectedLocation = null;
      this.pendingLocation = null;
      this.locationName = '';
    }
  }

  // Handle location search from the search box
  onSearchLocation(query: string): void {
    if (!query || !query.trim()) {
      return;
    }

    // You can implement a geocoding service call here to convert the query to coordinates
    // For now, we'll just log it and focus on the map interaction
    console.log('Searching for location:', query);
    
    // In a real implementation, you would call a geocoding service here
    // and then update the map with the coordinates
    // For example:
    // this.geocodingService.search(query).subscribe(coords => {
    //   if (coords) {
    //     this.selectedLocation = {
    //       latitude: coords.lat,
    //       longitude: coords.lng,
    //       radius: 5
    //     };
    //   }
    // });
  }

  // Clear the selected location
  clearLocation(): void {
    this.selectedLocation = null;
    this.pendingLocation = null;
    this.locationFilter = '';
    this.locationName = '';
    this.page = 0;
    this.onSearch();
  }

  constructor(
    private bookService: BookService,
    private router: Router,
    private categoryService: CategoryService,
    private http: HttpClient
  ) {
  }

  ngOnInit(): void {
    // Initialize modal state
    this.resetGuestModalState();
    
    // Load initial data
    this.categoryService.getAllCategories().subscribe(categories => {
      this.categories = categories;
    });
    this.findAllBooks();
  }
  
  /**
   * Reset the guest modal state
   */
  private resetGuestModalState(): void {
    this.guestModalVisible = false;
    this.guestModalBookId = undefined;
    this.guestModalBookTitle = '';
  }

  private findAllBooks() {
    this.isLoading = true;
    const params: any = {
      page: this.page,
      size: this.size
    };

    // Add search term if provided
    if (this.searchTerm && this.searchTerm.trim() !== '') {
      params.search = this.searchTerm.trim();
    }
    
    // Add location filter if provided
    if (this.locationFilter) {
      params.location = this.locationFilter;
    }
    
    // Add geolocation parameters if explicitly set by user
    if (this.selectedLocation?.latitude && this.selectedLocation?.longitude) {
      params.lat = this.selectedLocation.latitude;
      params.lng = this.selectedLocation.longitude;
      params.radius = this.selectedLocation.radius || 5; // Default 5km radius
    }

    // Add category filter if provided
    if (this.selectedCategoryId) {
      params.categoryId = this.selectedCategoryId;
    }

    this.bookService.findAllBooks(params).subscribe({
      next: (response) => {
        // Ensure we have a valid response with content
        if (!response || !response.content) {
          this.bookResponse = { content: [] };
          this.pages = [0];
          this.isLoading = false;
          return;
        }
        
        this.bookResponse = response;
        this.pages = Array(response.totalPages || 0)
          .fill(0)
          .map((_, i) => i);
        
        // If we got an empty page but there are results, go back to first page
        if (response.content && response.content.length === 0 && response.totalElements && response.totalElements > 0) {
          this.page = 0;
          this.findAllBooks();
          return;
        }
        
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error fetching books:', err);
        
        // Handle 500 error specifically
        if (err.status === 500) {
          // If we're not on the first page, try going back to the first page
          if (this.page > 0) {
            this.page = 0;
            this.findAllBooks();
            return;
          }
          this.message = 'Server error while loading products. Please try again or contact support if the problem persists.';
        } else if (err.status === 404) {
          this.message = 'No products found matching your criteria.';
          this.bookResponse = { content: [] };
          this.pages = [0];
        } else {
          this.message = 'Error loading products. Please check your connection and try again.';
        }
        
        this.level = 'error';
        this.isLoading = false;
      }
    });
  }

  gotToPage(page: number) {
    this.page = page;
    this.findAllBooks();
  }

  goToFirstPage() {
    this.page = 0;
    this.findAllBooks();
  }

  goToPreviousPage() {
    this.page --;
    this.findAllBooks();
  }

  goToLastPage() {
    this.page = this.bookResponse.totalPages as number - 1;
    this.findAllBooks();
  }

  goToNextPage() {
    this.page++;
    this.findAllBooks();
  }

  get isLastPage() {
    return this.page === this.bookResponse.totalPages as number - 1;
  }

  // TrackBy function for better performance
  trackByBookId(index: number, book: BookResponse): number {
    return book.id || index;
  }



  borrowBook(book: BookResponse) {
    this.message = '';
    this.level = 'success';
    this.bookService.borrowBook({
      'book-id': book.id as number
    }).subscribe({
      next: () => {
        this.level = 'success';
        this.message = 'Book successfully added to your list';
      },
      error: (err) => {
        this.level = 'error';
        // Handle different types of errors gracefully
        if (err.error && err.error.error) {
          this.message = err.error.error;
        } else if (err.error && err.error.message) {
          this.message = err.error.message;
        } else if (err.message) {
          this.message = err.message;
        } else {
          this.message = 'Unable to borrow book. Please try again.';
        }
      }
    });
  }

  displayBookDetails(book: BookResponse) {
    if (book && book.id != null) {
      this.router.navigate(['products', 'details', book.id]);
    } else {
      this.message = 'Book details are unavailable. Please try again.';
      this.level = 'error';
    }
  }

  onCategoryChange() {
    this.page = 0;
    this.findAllBooks();
  }

  /**
   * Handles search functionality when user searches for books by name
   * Resets pagination and applies search filters
   */
  onSearch() {
    this.page = 0;
    this.findAllBooks();
  }

  onGuestRent(event: {bookId: number, data: any}) {
    this.http.post('/api/v1/guest-rent', { bookId: event.bookId, ...event.data }).subscribe({
      next: () => this.showSuccessNotificationMessage('🎉 Request sent successfully! We\'ll contact you soon.'),
      error: () => this.showSuccessNotificationMessage('❌ Failed to send request. Please try again.')
    });
  }

  onGuestRentModalOpen(event: { bookId: number, bookTitle: string }) {
    // Only open if we have valid book data
    if (event?.bookId && event?.bookTitle) {
      this.guestModalBookId = event.bookId;
      this.guestModalBookTitle = event.bookTitle;
      this.guestModalVisible = true;
    }
  }

  showSuccessNotificationMessage(message: string) {
    this.successMessage = message;
    this.showSuccessNotification = true;
    
    // Auto-hide after 4 seconds
    setTimeout(() => {
      this.showSuccessNotification = false;
    }, 4000);
  }

  onGuestRentModalClose() {
    this.resetGuestModalState();
  }

  onGuestRentModalSave(data: any) {
    if (this.guestModalBookId) {
      this.onGuestRent({ bookId: this.guestModalBookId, data });
    }
    this.onGuestRentModalClose();
  }

  clearFilters() {
    this.searchTerm = '';
    this.selectedCategoryId = undefined;
    this.locationFilter = '';
    this.selectedLocation = null;
    this.page = 0;
    this.findAllBooks();
    this.searchTerm = '';
    this.selectedCategoryId = undefined;
    this.locationFilter = '';
    this.selectedLocation = null;
    
    // Reset pagination
    this.page = 0;
    
    // Reload books without any filters
    this.findAllBooks();
  }

  get filteredBooks() {
    let books = this.bookResponse.content || [];
    if (this.selectedCategoryId) {
      books = books.filter(book => book.categoryId === this.selectedCategoryId);
    }
    if (this.searchTerm && this.searchTerm.trim() !== '') {
      const term = this.searchTerm.trim().toLowerCase();
      books = books.filter(book => (book.title || '').toLowerCase().includes(term));
    }
    return books;
  }

}
