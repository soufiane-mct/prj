import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'customDate',
  standalone: true
})
export class CustomDatePipe implements PipeTransform {
  transform(value: any, format: string = 'short'): string | null {
    if (!value) return null;
    
    // If value is an array of numbers (e.g., [2025,8,9,3,23,4,502815000])
    if (Array.isArray(value)) {
      // Create a new Date object using the array values
      // Note: JavaScript months are 0-indexed, so we don't need to adjust the month
      const [year, month, day, hours, minutes, seconds, ms] = value;
      const date = new Date(year, month, day, hours, minutes, seconds, ms);
      return this.formatDate(date, format);
    }
    
    // If value is a string or number that can be parsed to a date
    const date = new Date(value);
    if (!isNaN(date.getTime())) {
      return this.formatDate(date, format);
    }
    
    return null;
  }
  
  private formatDate(date: Date, format: string): string {
    const options: Intl.DateTimeFormatOptions = {};
    
    switch (format) {
      case 'short':
        return date.toLocaleString();
      case 'date':
        return date.toLocaleDateString();
      case 'time':
        return date.toLocaleTimeString();
      case 'iso':
        return date.toISOString();
      default:
        return date.toLocaleString();
    }
  }
}
