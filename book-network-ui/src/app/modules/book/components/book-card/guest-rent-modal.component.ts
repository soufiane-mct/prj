import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-guest-rent-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './guest-rent-modal.component.html',
  styleUrl: './guest-rent-modal.component.scss'
})
export class GuestRentModalComponent {
  @Input() show = false;
  @Input() bookTitle = '';
  @Output() close = new EventEmitter<void>();
  @Output() save = new EventEmitter<any>();

  fullName = '';
  phone = '';
  location = '';

  onSave() {
    // Split the full name into first name and last name
    const nameParts = this.fullName.trim().split(' ');
    const firstName = nameParts[0] || '';
    const lastName = nameParts.length > 1 ? nameParts.slice(1).join(' ') : '';
    
    this.save.emit({ 
      name: firstName,
      lastname: lastName,
      phone: this.phone, 
      location: this.location 
    });
  }

  formatPhoneNumber(event: any) {
    // Remove all non-numeric characters
    let value = event.target.value.replace(/\D/g, '');
    // Limit to 9 digits
    if (value.length > 9) {
      value = value.substring(0, 9);
    }
    this.phone = value;
  }

  validateLocation(event: any) {
    // Basic validation - ensure it contains a comma (city, state format)
    const value = event.target.value;
    if (value.length > 0 && !value.includes(',')) {
      // You could add more sophisticated validation here
      // For now, we'll just ensure it's not empty and has reasonable length
    }
  }
} 