import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SharedModule } from '../../shared/shared.module';

// Standalone Components
import { MainComponent } from './pages/main/main.component';
import { BookListComponent } from './pages/book-list/book-list.component';
import { MyBooksComponent } from './pages/my-books/my-books.component';
import { ManageBookComponent } from './pages/manage-book/manage-book.component';
import { BorrowedBookListComponent } from './pages/borrowed-book-list/borrowed-book-list.component';
import { ReturnBooksComponent } from './pages/return-books/return-books.component';
import { BookDetailsComponent } from './pages/book-details/book-details.component';

// Routes
import { BOOK_ROUTES } from './book.routes';

@NgModule({
  declarations: [
    // No declarations for standalone components
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    SharedModule,
    RouterModule.forChild(BOOK_ROUTES),
    MainComponent,
    BookListComponent,
    MyBooksComponent,
    ManageBookComponent,
    BorrowedBookListComponent,
    ReturnBooksComponent,
    BookDetailsComponent
  ]
})
export class BookModule {}
