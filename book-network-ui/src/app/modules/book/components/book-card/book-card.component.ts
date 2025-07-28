import { Component,EventEmitter, Input, Output } from '@angular/core';
import { BookResponse } from '../../../../services/models/book-response';
import { CommonModule } from '@angular/common';
import { RatingComponent } from "../rating/rating.component";
import { GuestRentModalComponent } from './guest-rent-modal.component';
import { TokenService } from '../../../../services/token/token.service';

@Component({
  selector: 'app-book-card',
  imports: [CommonModule, RatingComponent],
  templateUrl: './book-card.component.html',
  styleUrl: './book-card.component.scss'
})
export class BookCardComponent {
  private _book: BookResponse = {};
  private _manage = false;
  private _bookCover: string | undefined;

  constructor(private tokenService: TokenService) {}

  get bookCover(): string | undefined {
    if (this._book.cover) {
      //hdi d book cover
      return 'data:image/jpg;base64,' + this._book.cover
    }
    return 'https://source.unsplash.com/user/c_v_r/1900x800';
  }

  get book(): BookResponse {
    return this._book;
  }

  @Input()
  set book(value: BookResponse) {
    this._book = value; //drr bsh tjib data dl book
  }


  get manage(): boolean {
    return this._manage;
  }

  @Input()
  set manage(value: boolean) {
    this._manage = value;
  }

  @Output() private share: EventEmitter<BookResponse> = new EventEmitter<BookResponse>();
  @Output() private archive: EventEmitter<BookResponse> = new EventEmitter<BookResponse>();
  @Output() private borrow: EventEmitter<BookResponse> = new EventEmitter<BookResponse>();
  @Output() private edit: EventEmitter<BookResponse> = new EventEmitter<BookResponse>();
  @Output() private details: EventEmitter<BookResponse> = new EventEmitter<BookResponse>();
  @Output() private delete: EventEmitter<BookResponse> = new EventEmitter<BookResponse>();
  @Output() guestRent = new EventEmitter<{bookId: number, data: any}>();
  @Output() guestRentModalOpen = new EventEmitter<{ bookId: number, bookTitle: string }>();

  onShare() {
    this.share.emit(this._book);
  }

  // onArchive() {
  //   this.archive.emit(this._book);
  // }

  onBorrow() {
    if (!this.tokenService.token) {
      this.guestRentModalOpen.emit({ bookId: this._book.id!, bookTitle: this._book.title || '' });
    } else {
      this.borrow.emit(this._book);
    }
  }

  onEdit() {
    this.edit.emit(this._book);
  }

  onShowDetails() {
    this.details.emit(this._book);
  }

  onDelete() {
    this.delete.emit(this._book);
  }
}
