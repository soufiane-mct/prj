import { Component, OnInit } from '@angular/core';
import { BookRequest } from '../../../../services/models/book-request';
import { BookService } from '../../../../services/services/book.service';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-manage-book',
  imports: [CommonModule, FormsModule,RouterModule],
  templateUrl: './manage-book.component.html',
  styleUrl: './manage-book.component.scss'
})
export class ManageBookComponent implements OnInit {

  errorMsg: Array<string> = [];
  bookRequest: BookRequest = {
    authorName: '',
    isbn: '',
    synopsis: '',
    title: ''
  };
  selectedBookCover: any;
  selectedPicture: string | undefined;

  constructor(
    private bookService: BookService,
    private router: Router,
    private activatedRoute: ActivatedRoute
  ) {
  }

  ngOnInit(): void {
    const bookId = this.activatedRoute.snapshot.params['bookId'];
    if (bookId) {
      this.bookService.findBookById({
        'book-id': bookId
      }).subscribe({
        next: (book) => {
         this.bookRequest = {
           id: book.id,
           title: book.title as string,
           authorName: book.authorName as string,
           isbn: book.isbn as string,
           synopsis: book.synopsis as string,
           shareable: book.shareable
         };
         this.selectedPicture='data:image/jpg;base64,' + book.cover;
        }
      });
    }
  }

  saveBook() {
    console.log('DEBUG: Starting saveBook method');
    this.bookService.saveBook({
      body: this.bookRequest
    }).subscribe({
      next: (bookId) => {
        console.log('DEBUG: Book saved with ID:', bookId);
        if (this.selectedBookCover) {
          console.log('DEBUG: About to upload cover for book ID:', bookId);
          console.log('DEBUG: Selected file:', this.selectedBookCover);
          this.bookService.uploadBookCoverPictureRaw(bookId, this.selectedBookCover)
            .subscribe({
              next: () => {
                console.log('DEBUG: Cover upload successful');
                this.router.navigate(['/books/my-books']);
              },
              error: (err) => {
                console.error('Cover upload failed:', err);
                this.errorMsg = ['Cover upload failed.'];
                this.router.navigate(['/books/my-books']);
              }
            });
        } else {
          console.log('DEBUG: No cover selected, navigating to my-books');
          this.router.navigate(['/books/my-books']);
        }
      },
      error: (err) => {
        console.log(err.error);
        this.errorMsg = err.error.validationErrors;
      }
    });
  }

  onFileSelected(event: any) {
    this.selectedBookCover = event.target.files[0];
    console.log(this.selectedBookCover);

    if (this.selectedBookCover) {

      const reader = new FileReader();
      reader.onload = () => {
        this.selectedPicture = reader.result as string;
      };
      reader.readAsDataURL(this.selectedBookCover);
    }
  }
}
