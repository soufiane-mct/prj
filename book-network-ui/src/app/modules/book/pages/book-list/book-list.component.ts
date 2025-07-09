import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {Router} from '@angular/router';
import { PageResponseBookResponse } from '../../../../services/models/page-response-book-response';
import { BookService } from '../../../../services/services/book.service';
import { BookResponse } from '../../../../services/models/book-response';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { HttpTokenInterceptor } from '../../../../services/interceptor/http-token.interceptor';
import { BookCardComponent } from "../../components/book-card/book-card.component";



@Component({
  selector: 'app-book-list',
  imports: [CommonModule, BookCardComponent],
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

  constructor(
    private bookService: BookService,
    private router: Router
  ) {
  }

  ngOnInit(): void {
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


}
