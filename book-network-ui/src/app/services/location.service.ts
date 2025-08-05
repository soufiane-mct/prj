import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export interface LocationData {
  latitude?: number;
  longitude?: number;
  address?: string;
  fullAddress?: string;
  locationName?: string;
}

@Injectable({
  providedIn: 'root'
})
export class LocationService {
  private currentLocation = new BehaviorSubject<LocationData | null>(null);
  currentLocation$ = this.currentLocation.asObservable();

  constructor() {}

  updateLocation(location: LocationData | null): void {
    this.currentLocation.next(location);
  }

  formatLocation(location: LocationData | null): string {
    if (!location) return 'No location selected';
    
    if (location.fullAddress) {
      return location.fullAddress;
    }
    
    if (location.latitude !== undefined && location.longitude !== undefined) {
      return `Lat: ${location.latitude.toFixed(4)}, Lng: ${location.longitude.toFixed(4)}`;
    }
    
    return location.address || location.locationName || 'No location selected';
  }

  getCurrentLocation(): LocationData | null {
    return this.currentLocation.value;
  }
}
