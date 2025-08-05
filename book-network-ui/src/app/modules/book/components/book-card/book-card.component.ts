import { Component, EventEmitter, Input, Output } from '@angular/core';
import { BookResponse } from '../../../../services/models/book-response';
import { CommonModule } from '@angular/common';
import { RatingComponent } from "../rating/rating.component";
import { GuestRentModalComponent } from './guest-rent-modal.component';
import { TokenService } from '../../../../services/token/token.service';
import { LocationService } from '../../../../shared/services/location.service';

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

  constructor(
    private tokenService: TokenService,
    private locationService: LocationService
  ) {}

  get bookCover(): string | undefined {
    if (this._book.cover) {
      return 'data:image/jpg;base64,' + this._book.cover;
    }
    return 'https://source.unsplash.com/user/c_v_r/1900x800';
  }

  get formattedLocation(): string {
    if (!this._book) return '';
    
    return this.locationService.formatLocation({
      latitude: this._book.latitude,
      longitude: this._book.longitude,
      fullAddress: this._book.fullAddress,
      address: this._book.location,
      locationName: this._book.location
    });
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
