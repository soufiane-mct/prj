import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';

export interface LocationData {
  lat?: number;
  lng?: number;
  latitude?: number;
  longitude?: number;
  radius?: number;
  address?: string;
  locationName?: string;
  fullAddress?: string;
}

@Injectable({
  providedIn: 'root'
})
export class LocationService {
  private currentLocation = new BehaviorSubject<LocationData | null>(null);

  constructor(private http: HttpClient) { }

  // Get current location as observable
  getCurrentLocation(): Observable<LocationData | null> {
    return this.currentLocation.asObservable();
  }

  // Update the current location
  updateLocation(location: LocationData | null): void {
    this.currentLocation.next(location);
  }

  // Format location for display in a user-friendly way
  formatLocation(location: LocationData | null): string {
    if (!location) return 'Location not specified';

    // First, try to use the most descriptive available field
    if (location.fullAddress) {
      const parts = location.fullAddress.split(',');
      if (parts.length > 2) {
        return `${parts[0].trim()}, ${parts[1].trim()}`;
      }
      return location.fullAddress;
    }

    if (location.address) {
      return location.address;
    }

    if (location.locationName) {
      return location.locationName;
    }

    // If we only have coordinates, try to fetch address (reverse geocode)
    const lat = location.lat ?? location.latitude;
    const lng = location.lng ?? location.longitude;

    if (lat !== undefined && lng !== undefined) {
      // Optionally, you can trigger reverse geocoding here and update the location object
      // But since this is a sync method, you should use getAddressFromCoordinates() elsewhere
      const latDir = lat >= 0 ? 'N' : 'S';
      const lngDir = lng >= 0 ? 'E' : 'W';
      const absLat = Math.abs(Number(lat)).toFixed(4);
      const absLng = Math.abs(Number(lng)).toFixed(4);
      return `${absLat}°${latDir}, ${absLng}°${lngDir}`;
    }

    return 'Location not specified';
  }

  /**
   * Reverse geocode coordinates to get address (city, street, etc.)
   * Uses OpenStreetMap Nominatim API (no key required, but rate-limited)
   * Returns an Observable with address string or null
   */
  getAddressFromCoordinates(lat: number, lng: number): Observable<string | null> {
    const url = `https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${lat}&lon=${lng}`;
    return new Observable<string | null>(observer => {
      this.http.get<any>(url, { headers: { 'Accept-Language': 'en', 'User-Agent': 'book-network-ui/1.0' } }).subscribe({
        next: (result) => {
          if (result && result.address) {
            // Try to build a nice address: street, city
            const street = result.address.road || result.address.pedestrian || result.address.footway || '';
            const city = result.address.city || result.address.town || result.address.village || result.address.hamlet || '';
            const display = [street, city].filter(Boolean).join(', ');
            observer.next(display || result.display_name || null);
          } else {
            observer.next(null);
          }
          observer.complete();
        },
        error: () => {
          observer.next(null);
          observer.complete();
        }
      });
    });
  }

  // Extract coordinates from location object
  getCoordinates(location: LocationData): { lat: number; lng: number } | null {
    const lat = location.lat ?? location.latitude;
    const lng = location.lng ?? location.longitude;
    if (lat !== undefined && lng !== undefined) {
      return { lat, lng };
    }
    return null;
  }
}
