import { Component, OnInit } from '@angular/core';
import { BookService } from '../../../../services/services/book.service';
import { CommonModule, DatePipe } from '@angular/common';

@Component({
  selector: 'app-guest-rent-requests',
  standalone: true,
  imports: [CommonModule, DatePipe],
  templateUrl: './guest-rent-requests.component.html',
  styleUrls: ['./guest-rent-requests.component.scss']
})
export class GuestRentRequestsComponent implements OnInit {
  requests: any[] = [];
  loading = true;
  error = '';

  constructor(private bookService: BookService) {}

  ngOnInit(): void {
    this.bookService.getOwnerGuestRentRequests().subscribe({
      next: (data) => {
        this.requests = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load guest rent requests.';
        this.loading = false;
      }
    });
  }
} 