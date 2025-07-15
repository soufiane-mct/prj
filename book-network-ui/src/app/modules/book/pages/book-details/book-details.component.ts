import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink, RouterModule, Router } from '@angular/router';
import { BookResponse } from '../../../../services/models/book-response';
import { PageResponseFeedbackResponse } from '../../../../services/models/page-response-feedback-response';
import { BookService } from '../../../../services/services/book.service';
import { FeedbackService } from '../../../../services/services/feedback.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RatingComponent } from '../../components/rating/rating.component';

@Component({
  selector: 'app-book-details',
  imports: [CommonModule, FormsModule,RouterModule, RatingComponent],
  templateUrl: './book-details.component.html',
  styleUrl: './book-details.component.scss'
})
export class BookDetailsComponent implements OnInit {
  book: BookResponse = {};
  feedbacks: PageResponseFeedbackResponse = {};
  page = 0;
  size = 5;
  pages: any = [];
  private bookId = 0;

  constructor(
    private bookService: BookService,
    private feedbackService: FeedbackService,
    private activatedRoute: ActivatedRoute,
    private router: Router
  ) {
  }
  
  ngOnInit(): void {
    this.bookId = this.activatedRoute.snapshot.params['bookId'];
    if (this.bookId) {
      this.bookService.findBookById({
        'book-id': this.bookId
      }).subscribe({
        next: (book) => {
          this.book = book;
          this.findAllFeedbacks();
        }
      });
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
        console.log('Could not load feedbacks:', error);
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
    // Implement borrow functionality
    console.log('Borrowing book:', this.book.title);
    // You can add navigation to borrow page or show a modal
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

}
