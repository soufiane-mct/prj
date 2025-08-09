import { Component, OnInit } from '@angular/core';
import { BookService } from '../../../../services/services/book.service';
import { CommonModule, DatePipe } from '@angular/common';

export interface GuestRentRequest {
  id: number;
  name: string;
  lastname: string;
  phone: string;
  location: string;
  createdDate: string;
  bookTitle: string;
  bookAuthor: string;
}

@Component({
  selector: 'app-guest-rent-requests',
  standalone: true,
  imports: [CommonModule, DatePipe],
  templateUrl: './guest-rent-requests.component.html',
  styleUrls: ['./guest-rent-requests.component.scss']
})
export class GuestRentRequestsComponent implements OnInit {
  requests: GuestRentRequest[] = [];
  loading = true;
  error = '';

  constructor(private bookService: BookService) {}

  private parseDate(dateArray: any[]): string {
    try {
      // Check if the input is a valid date array
      if (!Array.isArray(dateArray) || dateArray.length < 7) {
        return new Date().toISOString();
      }
      
      // Create a new Date object using the array values
      // Note: Month is 0-indexed in JavaScript Date
      const [year, month, day, hours, minutes, seconds, milliseconds] = dateArray;
      const date = new Date(year, month - 1, day, hours, minutes, seconds, milliseconds / 1000000);
      
      // Return ISO string if valid date, otherwise return current date
      return isNaN(date.getTime()) ? new Date().toISOString() : date.toISOString();
    } catch (e) {
      console.warn('Error parsing date:', e);
      return new Date().toISOString();
    }
  }

  ngOnInit(): void {
    this.bookService.getOwnerGuestRentRequests().subscribe({
      next: (data: any[]) => {
        // Map the data to ensure it matches our interface
        this.requests = data.map(req => ({
          id: req.id,
          name: req.name || '',
          lastname: req.lastname || '',
          phone: req.phone || '',
          location: req.location || '',
          // Handle both string dates and date arrays
          createdDate: Array.isArray(req.createdDate) 
            ? this.parseDate(req.createdDate) 
            : (req.createdDate || new Date().toISOString()),
          bookTitle: req.bookTitle || 'Unknown Book',
          bookAuthor: req.bookAuthor || 'Unknown Author'
        }));
        
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading guest rent requests:', err);
        this.error = 'Failed to load guest rent requests. Please try again later.';
        this.loading = false;
      }
    });
  }
} 