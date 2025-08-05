import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';

import { LocationService, LocationData } from '../../../../shared/services/location.service';
import { BookService } from '../../../../services/services/book.service';
import { CategoryService, Category } from '../../../../services/services/category.service';
import { PageResponseBookResponse } from '../../../../services/models/page-response-book-response';
import { BookResponse } from '../../../../services/models/book-response';
import { BookCardComponent } from '../../components/book-card/book-card.component';
import { MapPickerComponent } from '../../../../shared/components/map-picker/map-picker.component';

@Component({
  selector: 'app-my-books',
  standalone: true,
  imports: [
    CommonModule, 
    RouterModule, 
    FormsModule,
    BookCardComponent,
    MapPickerComponent
  ],
  templateUrl: './my-books.component.html',
  styleUrls: ['./my-books.component.scss']
})
export class MyBooksComponent implements OnInit, OnDestroy {
  // Location filter properties
  showLocationModal = false;
  filterLat: number | undefined;
  filterLng: number | undefined;
  filterRadius = 10; // Default radius in km
  selectedLocation: LocationData | null = null;
  locationName: string = '';

  // Book data
  bookResponse: PageResponseBookResponse = {};
  books: BookResponse[] = [];
  
  // Pagination
  page = 0;
  size = 12;
  totalPages = 0;
  
  // UI State
  categories: Category[] = [];
  pages: number[] = [];
  selectedCategoryId: number | undefined;
  searchTerm: string = '';
  showSuccessNotification = false;
  successMessage = '';
  showErrorNotification = false;
  errorMessage = '';
  isLoading = true;
  
  // Search debounce
  private searchSubject = new Subject<string>();
  private destroy$ = new Subject<void>();

  constructor(
    private bookService: BookService,
    private router: Router,
    private categoryService: CategoryService,
    private locationService: LocationService
  ) {
    // Setup search debounce
    this.searchSubject.pipe(
      debounceTime(500),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(() => {
      this.page = 0; // Reset to first page on new search
      this.fetchBooks();
    });
  }

  async ngOnInit(): Promise<void> {
    try {
      await this.fetchBooks();
      
      // Load categories
      this.categoryService.getAllCategories().subscribe({
        next: (categories: Category[]) => {
          this.categories = categories;
        },
        error: (err: any) => {
          console.error('Error loading categories:', err);
          this.showError('Failed to load categories');
        }
      });
      
      // Load saved location from service if available
      this.locationService.getCurrentLocation().subscribe({
        next: (location: LocationData | null) => {
          if (location) {
            this.filterLat = location.lat || location.latitude;
            this.filterLng = location.lng || location.longitude;
            this.filterRadius = location.radius || 10;
            this.locationName = location.locationName || location.address || 'Selected location';
          }
        }
      });
    } catch (error) {
      console.error('Error initializing component:', error);
      this.showError('Failed to initialize component');
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // Location handling methods
  openLocationModal(): void {
    this.showLocationModal = true;
  }

  closeLocationModal(): void {
    this.showLocationModal = false;
  }

  applyLocationFilter(): void {
    this.closeLocationModal();
    this.onSearch();
  }

  onLocationSelected(locationData: { latitude: number; longitude: number; radius: number } | null): void {
    if (!locationData) {
      this.clearLocation();
      return;
    }

    this.filterLat = locationData.latitude;
    this.filterLng = locationData.longitude;
    this.filterRadius = locationData.radius || 10;
    this.locationName = `Location: ${locationData.latitude.toFixed(4)}, ${locationData.longitude.toFixed(4)}`;
    
    // Update the location service with the correct structure
    this.locationService.updateLocation({
      ...locationData,
      lat: locationData.latitude,
      lng: locationData.longitude
    });
    
    this.selectedLocation = locationData;
  }

  clearLocation(): void {
    this.filterLat = undefined;
    this.filterLng = undefined;
    this.filterRadius = 10;
    this.locationName = '';
    this.selectedLocation = null;
    this.locationService.updateLocation(null);
    this.onSearch();
  }

  locationDisplay(): string {
    if (this.locationName) {
      return this.locationName;
    }
    if (this.filterLat !== undefined && this.filterLng !== undefined) {
      return this.locationService.formatLocation({
        lat: this.filterLat,
        lng: this.filterLng,
        radius: this.filterRadius
      });
    }
    return '';
  }

  private updatePagination(totalPages: number): void {
    this.pages = Array.from({length: totalPages}, (_, i) => i);
  }

  private fetchBooks(): Promise<void> {
    this.isLoading = true;
    return new Promise((resolve) => {
      this.bookService.findAllBooksByOwner({
        page: this.page,
        size: this.size,
        lat: this.filterLat,
        lng: this.filterLng,
        radius: this.filterRadius
      }).subscribe({
        next: (books: PageResponseBookResponse) => {
          this.bookResponse = books;
          this.books = books.content || [];
          this.totalPages = books.totalPages || 0;
          this.updatePagination(this.totalPages);
          this.isLoading = false;
          resolve();
        },
        error: (err: any) => {
          console.error('Error fetching books:', err);
          this.showError('Failed to load books. Please try again.');
          this.isLoading = false;
          resolve();
        }
      });
    });
  }

  onSearchInput(): void {
    this.searchSubject.next(this.searchTerm);
  }

  onSearch(): void {
    this.page = 0; // Reset to first page on new search
    this.fetchBooks().catch((err: any) => {
      console.error('Error in search:', err);
      this.showError('An error occurred during search');
    });
  }

  gotToPage(page: number) {
    this.page = page;
    this.fetchBooks();
  }

  goToFirstPage() {
    this.page = 0;
    this.fetchBooks();
  }

  goToPreviousPage() {
    this.page --;
    this.fetchBooks();
  }

  goToLastPage() {
    this.page = this.bookResponse.totalPages as number - 1;
    this.fetchBooks();
  }

  goToNextPage() {
    this.page++;
    this.fetchBooks();
  }

  get isLastPage() {
    return this.page === this.bookResponse.totalPages as number - 1;
  }

  archiveBook(book: BookResponse) {
    this.bookService.updateArchivedStatus({
      'book-id': book.id as number
    }).subscribe({
      next: () => {
        book.archived = !book.archived;
      }
    });
  }

  shareBook(book: BookResponse) {
    this.bookService.updateShareableStatus({
      'book-id': book.id as number
    }).subscribe({
      next: () => {
        book.shareable = !book.shareable;
      }
    });
  }

  editBook(book: BookResponse) {
    if (book && book.id != null) {
      this.router.navigate(['products', 'manage', book.id]);
    } else {
      this.errorMessage = 'Book details are unavailable. Please try again.';
      this.showErrorNotification = true;
    }
  }

  showDeleteModal: boolean = false;
  bookToDelete: BookResponse | null = null;

  openDeleteModal(book: BookResponse) {
    this.bookToDelete = book;
    this.showDeleteModal = true;
  }

  confirmDelete() {
    if (!this.bookToDelete) return;
    this.bookService.deleteBook({ 'book-id': this.bookToDelete.id as number }).subscribe({
      next: () => {
        this.showSuccess('Product deleted successfully!');
        this.fetchBooks();
      },
      error: (err) => {
        let msg = 'Unable to delete product. Please try again.';
        if (err.error && err.error.error) {
          msg = err.error.error;
        } else if (err.error && err.error.message) {
          msg = err.error.message;
        } else if (err.message) {
          msg = err.message;
        }
        this.showError(msg);
      },
      complete: () => {
        this.showDeleteModal = false;
        this.bookToDelete = null;
      }
    });
  }

  cancelDelete() {
    this.showDeleteModal = false;
    this.bookToDelete = null;
  }

  private showSuccess(message: string): void {
    this.successMessage = message;
    this.showSuccessNotification = true;
    setTimeout(() => this.showSuccessNotification = false, 5000);
  }
  
  private showError(message: string): void {
    this.errorMessage = message;
    this.showErrorNotification = true;
    setTimeout(() => this.showErrorNotification = false, 5000);
  }

  onCategoryChange() {
    this.page = 0;
    this.fetchBooks().catch((err: any) => {
      console.error('Error changing category:', err);
      this.showError('Failed to filter by category');
    }).catch((err: any) => {
      console.error('Error changing category:', err);
      this.showError('Failed to filter by category');
    });
  }

  // onSearch is already implemented above

  get filteredBooks(): BookResponse[] {
    if (!this.books) return [];
    
    let filtered = [...this.books];
    
    // Apply category filter if a category is selected
    if (this.selectedCategoryId) {
      filtered = filtered.filter(book => book.categoryId === this.selectedCategoryId);
    }
    
    // Apply search filter if there's a search term
    if (this.searchTerm && this.searchTerm.trim() !== '') {
      const searchTerm = this.searchTerm.toLowerCase().trim();
      filtered = filtered.filter(book => 
        (book.title?.toLowerCase().includes(searchTerm)) ||
        (book.authorName?.toLowerCase().includes(searchTerm)) ||
        (book.synopsis?.toLowerCase().includes(searchTerm))
      );
    }
    
    return filtered;
  }
}
