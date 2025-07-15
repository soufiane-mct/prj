import { Component, OnInit } from '@angular/core';
import { PageResponseBookResponse } from '../../../../services/models/page-response-book-response';
import { BookService } from '../../../../services/services/book.service';
import { Router, RouterModule } from '@angular/router';
import { BookResponse } from '../../../../services/models/book-response';
import { BookCardComponent } from "../../components/book-card/book-card.component";
import { CommonModule } from '@angular/common';
import { CategoryService, Category } from '../../../../services/services/category.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-my-books',
  imports: [BookCardComponent, CommonModule, RouterModule, FormsModule],
  templateUrl: './my-books.component.html',
  styleUrl: './my-books.component.scss'
})
export class MyBooksComponent implements OnInit {

  bookResponse: PageResponseBookResponse = {};
  page = 0;
  size = 5;
  pages: any = [];
  categories: Category[] = [];
  selectedCategoryId: number | undefined;
  searchTerm: string = '';

  constructor(
    private bookService: BookService,
    private router: Router,
    private categoryService: CategoryService
  ) {
  }

  ngOnInit(): void {
    this.categoryService.getAllCategories().subscribe(categories => {
      this.categories = categories;
    });
    this.findAllBooks();
  }

  private findAllBooks() {
    this.bookService.findAllBooksByOwner({
      page: this.page,
      size: this.size
    })
      .subscribe({
        next: (books) => {
          this.bookResponse = books;
          const totalPages = this.bookResponse.totalPages ?? 0;
          this.pages = Array(this.bookResponse.totalPages)
            .fill(0)
            .map((x, i) => i);
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
    this.router.navigate(['products', 'manage', book.id]);
  }

  onCategoryChange() {
    // Triggers filteredBooks getter
  }

  onSearch() {
    // Triggers filteredBooks getter
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
