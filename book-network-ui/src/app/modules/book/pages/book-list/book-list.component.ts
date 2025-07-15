import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {Router} from '@angular/router';
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


@Component({
  selector: 'app-book-list',
  imports: [CommonModule, BookCardComponent, FormsModule, GuestRentModalComponent],
  // providers: [{
  //   provide: HTTP_INTERCEPTORS,
  //   useClass: HttpTokenInterceptor,
  //   multi: true 
  // }],
  templateUrl: './book-list.component.html',
  styleUrl: './book-list.component.scss',
})
export class BookListComponent implements OnInit {
  bookResponse: PageResponseBookResponse = {};
  page = 0;
  size = 5;
  pages: any = [];
  message = '';
  level: 'success' |'error' = 'success';
  Math = Math; // Make Math available in template
  categories: Category[] = [];
  selectedCategoryId: number | undefined;
  searchTerm: string = '';
  guestModalVisible = false;
  guestModalBookTitle = '';
  guestModalBookId: number | undefined;
  
  // Success notification properties
  showSuccessNotification = false;
  successMessage = '';

  constructor(
    private bookService: BookService,
    private router: Router,
    private categoryService: CategoryService,
    private http: HttpClient
  ) {
  }

  ngOnInit(): void {
    this.categoryService.getAllCategories().subscribe(categories => {
      this.categories = categories;
    });
    this.findAllBooks();
  }

  private findAllBooks() {
    this.bookService.findAllBooks({
      page: this.page,
      size: this.size
    }) 
      .subscribe({
        next: (books) => {
          this.bookResponse = books;//hdi li atji lina lbooks mn data
          this.pages = Array(this.bookResponse.totalPages)
            .fill(0)
            .map((x, i) => i);
        },
      

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
    this.router.navigate(['products', 'details', book.id]);
  }

  onCategoryChange() {
    // For backend filtering, call the backend here. For now, just triggers the getter.
  }

  onSearch() {
    // This will trigger Angular change detection and filteredBooks will update
    this.page = 0;
  }

  onGuestRent(event: {bookId: number, data: any}) {
    this.http.post('/api/v1/guest-rent', { bookId: event.bookId, ...event.data }).subscribe({
      next: () => this.showSuccessNotificationMessage('🎉 Request sent successfully! We\'ll contact you soon.'),
      error: () => this.showSuccessNotificationMessage('❌ Failed to send request. Please try again.')
    });
  }

  showSuccessNotificationMessage(message: string) {
    this.successMessage = message;
    this.showSuccessNotification = true;
    
    // Auto-hide after 4 seconds
    setTimeout(() => {
      this.showSuccessNotification = false;
    }, 4000);
  }

  onGuestRentModalOpen(event: { bookId: number, bookTitle: string }) {
    console.log('Modal open event received:', event);
    this.guestModalVisible = true;
    this.guestModalBookTitle = event.bookTitle;
    this.guestModalBookId = event.bookId;
    console.log('guestModalVisible:', this.guestModalVisible);
  }

  onGuestRentModalClose() {
    this.guestModalVisible = false;
    this.guestModalBookTitle = '';
    this.guestModalBookId = undefined;
  }

  onGuestRentModalSave(data: any) {
    if (this.guestModalBookId) {
      this.onGuestRent({ bookId: this.guestModalBookId, data });
    }
    this.onGuestRentModalClose();
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
