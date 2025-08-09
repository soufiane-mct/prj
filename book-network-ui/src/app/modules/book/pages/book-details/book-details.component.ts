import { Component, OnInit, ViewChild, ChangeDetectorRef, ElementRef } from '@angular/core';
import { ActivatedRoute, RouterLink, RouterModule, Router } from '@angular/router';
import { BookResponse } from '../../../../services/models/book-response';
import { PageResponseFeedbackResponse } from '../../../../services/models/page-response-feedback-response';
import { BookService } from '../../../../services/services/book.service';
import { FeedbackService } from '../../../../services/services/feedback.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RatingComponent } from '../../components/rating/rating.component';
import { TokenService } from '../../../../services/token/token.service';
import { GuestRentModalComponent } from '../../components/book-card/guest-rent-modal.component';
import { MapPickerComponent, LocationData } from '../../../../shared/components/map-picker/map-picker.component';
import { LocationService } from '../../../../shared/services/location.service';

@Component({
  selector: 'app-book-details',
  imports: [
    CommonModule, 
    FormsModule,
    RouterModule, 
    RatingComponent, 
    GuestRentModalComponent,
    MapPickerComponent
  ],
  templateUrl: './book-details.component.html',
  styleUrl: './book-details.component.scss'
})
export class BookDetailsComponent implements OnInit {
  book: BookResponse = {};
  bookImages: any[] = [];
  currentImageIndex = 0;
  showLocationModal = false;
  feedbacks: PageResponseFeedbackResponse = {};
  page = 0;
  size = 5;
  pages: any = [];
  private bookId = 0;
  private _guestModalVisible = false;
  get guestModalVisible(): boolean {
    return this._guestModalVisible;
  }
  set guestModalVisible(value: boolean) {
    this._guestModalVisible = value;
  }
  guestModalBookTitle = '';
  showSuccessNotification = false;
  successMessage = '';
  resolvedAddress: string | null = null;
  isResolvingAddress = false;

  @ViewChild('mapPicker') mapPicker!: MapPickerComponent;
  @ViewChild('videoPlayer') videoPlayer!: ElementRef<HTMLVideoElement>;
  
  // Default coordinates (Rabat, Morocco)
  defaultLocation: LocationData = {
    lat: 34.0209,
    lng: -6.8416,
    zoom: 12
  };
  
  get bookLocation(): LocationData {
    if (!this.book || this.book.latitude === undefined || this.book.longitude === undefined) {
      return this.defaultLocation;
    }
    
    return {
      lat: this.book.latitude,
      lng: this.book.longitude,
      zoom: 15,
      address: this.book.fullAddress || this.book.location || '',
      displayName: this.book.location || 'Product Location'
    };
  }

  constructor(
    private bookService: BookService,
    private feedbackService: FeedbackService,
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private tokenService: TokenService,
    private locationService: LocationService,
    private cdr: ChangeDetectorRef
  ) {
  }
  
  ngOnInit(): void {
    // Ensure all modals are closed when component initializes
    this.closeAllModals();
    
    this.bookId = this.activatedRoute.snapshot.params['bookId'];
    if (this.bookId) {
      this.bookService.findBookById({
        'book-id': this.bookId
      }).subscribe({
        next: (book) => {
          this.book = book;
          this.resolveBookAddress();
          this.findAllFeedbacks();
          this.loadBookImages();
        }
      });
    }
  }

  onVideoPlay() {
    // Hide any poster image when video starts playing
    if (this.videoPlayer) {
      this.videoPlayer.nativeElement.style.objectFit = 'contain';
    }
  }

  private resolveBookAddress() {
    // If fullAddress is present, use it. Otherwise, try to resolve from coordinates.
    if (this.book.fullAddress) {
      this.resolvedAddress = this.book.fullAddress;
    } else if (this.book.latitude != null && this.book.longitude != null) {
      this.isResolvingAddress = true;
      this.locationService.getAddressFromCoordinates(this.book.latitude, this.book.longitude).subscribe(addr => {
        this.resolvedAddress = addr;
        this.isResolvingAddress = false;
        this.cdr.markForCheck();
      });
    } else {
      this.resolvedAddress = null;
    }
  }

  private findAllFeedbacks() {
    this.feedbackService.findAllFeedbackByBook({
      'book-id': this.bookId,
      page: this.page,
      size: this.size
    }).subscribe({
      next: (data) => {
        this.feedbacks = data;
        this.generatePages();
      },
      error: (error) => {
        // Error loading feedbacks
        // Initialize empty feedbacks for guests
        this.feedbacks = {
          content: [],
          totalPages: 0,
          totalElements: 0,
          size: this.size,
          number: this.page
        };
        this.generatePages();
      }
    });
  }

  private generatePages() {
    this.pages = [];
    if (this.feedbacks.totalPages) {
      for (let i = 0; i < this.feedbacks.totalPages; i++) {
        this.pages.push(i);
      }
    }
  }

  goBack() {
    this.router.navigate(['/books']);
  }

  onBorrow() {
    const isLoggedIn = this.tokenService.isLogged();
    if (!isLoggedIn) {
      this.guestModalVisible = true;
      this.guestModalBookTitle = this.book.title || '';
      return;
    }
    if (!this.bookId) return;
    this.bookService.borrowBook({ 'book-id': this.bookId }).subscribe({
      next: () => {
        this.showSuccessNotificationMessage('Book successfully added to your list');
      },
      error: (err) => {
        let msg = 'Unable to borrow book. Please try again.';
        if (err.error && err.error.error) {
          msg = err.error.error;
        } else if (err.error && err.error.message) {
          msg = err.error.message;
        } else if (err.message) {
          msg = err.message;
        }
        this.showSuccessNotificationMessage(msg);
      }
    });
  }

  onGuestRentModalClose() {
    this.resetGuestModalState();
  }

  private resetGuestModalState(): void {
    this.guestModalVisible = false;
    this.guestModalBookTitle = '';
  }

  showSuccessNotificationMessage(message: string) {
    this.successMessage = message;
    this.showSuccessNotification = true;
    setTimeout(() => {
      this.showSuccessNotification = false;
    }, 4000);
  }

  onGuestRentModalSave(data: any) {
    // You may need to adjust the API endpoint and payload as in the book-list
    this.onGuestRentModalClose();
    this.showSuccessNotificationMessage('Your rent request has been sent!');
  }

  onShare() {
    // Implement share functionality
    if (navigator.share) {
      navigator.share({
        title: this.book.title,
        text: `Check out this product: ${this.book.title}`,
        url: window.location.href
      });
    } else {
      // Fallback for browsers that don't support Web Share API
      navigator.clipboard.writeText(window.location.href);
      alert('Link copied to clipboard!');
    }
  }

  gotToPage(page: number) {
    this.page = page;
    this.findAllFeedbacks();
  }

  goToFirstPage() {
    this.page = 0;
    this.findAllFeedbacks();
  }

  goToPreviousPage() {
    this.page --;
    this.findAllFeedbacks();
  }

  goToLastPage() {
    this.page = this.feedbacks.totalPages as number - 1;
    this.findAllFeedbacks();
  }

  goToNextPage() {
    this.page++;
    this.findAllFeedbacks();
  }

  get isLastPage() {
    return this.page === this.feedbacks.totalPages as number - 1;
  }

  private closeAllModals() {
    console.log('Closing all modals');
    this.showLocationModal = false;
    this.guestModalVisible = false;
    document.body.style.overflow = '';
    document.body.style.paddingRight = '';
  }

  closeLocationModal() {
    this.showLocationModal = false;
    // Remove any added padding
    document.body.style.paddingRight = '';
    document.body.classList.remove('modal-open');
  }

  openLocationModal() {
    this.showLocationModal = true;
    this.resolveBookAddress(); // Always resolve when opening modal
    
    // Ensure the map is properly initialized with the book's location
    if (this.mapPicker && this.book) {
      // Use setTimeout to ensure the modal is fully rendered before initializing the map
      setTimeout(() => {
        if (this.book.latitude && this.book.longitude) {
          this.mapPicker.centerMap(this.book.latitude, this.book.longitude, this.book.location || 'Book Location');
        }
      }, 100);
    }
  }
  
  // Handle click on modal backdrop
  onBackdropClick(event: MouseEvent) {
    const target = event.target as HTMLElement;
    if (target.classList.contains('location-modal')) {
      this.closeLocationModal();
    }
  }

  onLocationSelected(event: { latitude: number; longitude: number; radius: number } | null) {
    if (event) {
      console.log('Location selected:', event);
      // Update the book's location if needed
      this.book.latitude = event.latitude;
      this.book.longitude = event.longitude;
    }
  }

  private loadBookImages() {
    this.bookService.getBookImages({
      bookId: this.bookId
    }).subscribe({
      next: (images: any[]) => {
        // First load all images
        if (images && images.length > 0) {
          this.bookImages = images.map(img => ({
            id: img.id,
            imageUrl: this.getImageUrl(img.imageUrl || ''),
            isCover: img.isCover,
            isVideo: false
          }));
        } else if (this.book.cover) {
          // Fallback to using the cover if no images are found
          const cover = Array.isArray(this.book.cover) ? this.book.cover[0] : this.book.cover;
          this.bookImages = [{
            id: 0,
            imageUrl: this.getImageUrl(cover),
            isCover: true,
            isVideo: false
          }];
        }
        
        // Add video as the last item if it exists (check both video and videoUrl properties)
        const videoUrl = this.book.videoUrl || 
                        (this.book.video && this.book.video.startsWith('pending_') 
                          ? `http://localhost:8081/books/${this.bookId}/videos/${this.book.video.substring(8)}`
                          : this.book.video);
        
        if (videoUrl) {
          this.bookImages.push({
            id: -1, // Special ID for video
            videoUrl: videoUrl,
            isVideo: true,
            thumbnail: this.book.cover ? (Array.isArray(this.book.cover) ? this.book.cover[0] : this.book.cover) : ''
          });
        }
      },
      error: (err) => {
        console.error('Error loading book images:', err);
        // Fallback to using the cover if available
        if (this.book.cover) {
          const cover = Array.isArray(this.book.cover) ? this.book.cover[0] : this.book.cover;
          this.bookImages = [{
            id: 0,
            imageUrl: this.getImageUrl(cover),
            isCover: true
          }];
        }
      }
    });
  }

  private getImageUrl(url: string): string {
    if (!url) return '';
    
    // If it's already a full URL, return as is
    if (typeof url === 'string' && (url.startsWith('http') || url.startsWith('data:image'))) {
      return url;
    }
    
    // If it's a relative path starting with /api, prepend the base URL
    if (typeof url === 'string' && url.startsWith('/api/')) {
      return `http://localhost:8081${url}`;
    }
    
    // If it's a base64 string without the data URL prefix, add it
    if (typeof url === 'string' && !url.startsWith('data:image/')) {
      return `data:image/jpg;base64,${url}`;
    }
    
    return url;
  }

  nextImage(): void {
    if (!this.bookImages || this.bookImages.length <= 1) return;
    this.currentImageIndex = (this.currentImageIndex + 1) % this.bookImages.length;
  }

  prevImage(): void {
    if (!this.bookImages || this.bookImages.length <= 1) return;
    this.currentImageIndex = (this.currentImageIndex - 1 + this.bookImages.length) % this.bookImages.length;
  }

  goToImage(index: number): void {
    if (this.bookImages && index >= 0 && index < this.bookImages.length) {
      this.currentImageIndex = index;
    }
  }

  openLocationModalWithScrollbarFix(): void {
    this.openLocationModal();
    const scrollbarWidth = window.innerWidth - document.documentElement.clientWidth;
    if (scrollbarWidth > 0) {
      document.body.style.paddingRight = `${scrollbarWidth}px`;
    }
  }
}
