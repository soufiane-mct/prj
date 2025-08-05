import { Component, AfterViewInit, OnDestroy, Input, Output, EventEmitter, ChangeDetectorRef, NgZone, PLATFORM_ID, Inject, ViewChild, ElementRef, OnInit } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import * as L from 'leaflet';
import { firstValueFrom } from 'rxjs';
import { icon, Marker, LatLngTuple, LatLng, LatLngExpression, LeafletMouseEvent } from 'leaflet';

declare global {
  interface Window {
    L: typeof L;
  }
}

// Define the structure for location data
export interface LocationData {
  lat?: number;
  lng?: number;
  latitude?: number;
  longitude?: number;
  zoom?: number;
  address?: string;
  displayName?: string;
  radius?: number;
  useMiles?: boolean;
  timestamp?: string;
}

// Define map bounds for Morocco as LatLngBoundsExpression
const MOROCCO_BOUNDS: L.LatLngBoundsExpression = [
  [27.6, -13.2], // SW corner
  [36.0, -0.9]   // NE corner
];

// Fix for marker icons
const iconRetinaUrl = 'assets/leaflet/images/marker-icon-2x.png';
const iconUrl = 'assets/leaflet/images/marker-icon.png';
const shadowUrl = 'assets/leaflet/images/marker-shadow.png';
const iconDefault = icon({
  iconRetinaUrl,
  iconUrl,
  shadowUrl,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  tooltipAnchor: [16, -28],
  shadowSize: [41, 41]
});
Marker.prototype.options.icon = iconDefault;

@Component({
  selector: 'app-map-picker',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './map-picker.component.html',
  styleUrls: ['./map-picker.component.scss']
})
export class MapPickerComponent implements OnInit, AfterViewInit, OnDestroy {
  @Input() showMap = false;
  @Input() initialLat = 34.0209;
  @Input() initialLng = -6.8416;
  @Input() initialRadius = 5;
  @Input() showRadiusControl = true;
  @Input() showSearchControls = true;
  @Input() draggableMarker = true;
  @ViewChild('mapContainer', { static: false }) mapContainerRef!: ElementRef<HTMLDivElement>;
  @Output() locationChange = new EventEmitter<LocationData>();
  @Output() locationSelected = new EventEmitter<{ latitude: number; longitude: number; radius: number } | null>();
  @Output() locationCleared = new EventEmitter<void>();
  @Output() radiusChange = new EventEmitter<number>();
  @Output() unitChange = new EventEmitter<boolean>();

  // Map state
  map: L.Map | null = null;
  marker: L.Marker | null = null;
  circle: L.Circle | null = null;
  resizeObserver: ResizeObserver | null = null;
  isMapInitialized = false;
  isDestroyed = false;
  isDragging = false;
  resizeDebounceTimer: number | null = null;
  cleanupFns: (() => void)[] = [];
  isBrowser: boolean;
  isLoading = false;
  errorMessage: string | null = null;
  searchQuery = '';
  isSearching = false;
  useMiles = true; // Default to miles
  radius = 10; // Default radius in kilometers
  selectedLocation: LocationData | null = null;

  private initializationAttempts = 0;

  constructor(
    @Inject(PLATFORM_ID) private platformId: Object,
    private cdr: ChangeDetectorRef,
    private zone: NgZone,
    private http: HttpClient
  ) {
    this.isBrowser = isPlatformBrowser(platformId);
  }

  /**
   * Retries map initialization when it fails
   */
  public retryMapInitialization(): void {
    if (this.initializationAttempts < 3) {
      this.initializationAttempts++;
      this.initializeMap();
    } else {
      this.showError('Failed to initialize map after multiple attempts. Please refresh the page.');
    }
  }

  /**
   * Handles radius change from the input
   */
  public onRadiusChange(newRadius: number): void {
    if (isNaN(newRadius) || newRadius <= 0) {
      return;
    }
    this.radius = newRadius;
    this.radiusChange.emit(newRadius);
    
    if (this.selectedLocation && this.selectedLocation.lat && this.selectedLocation.lng) {
      this.updateCircle(this.selectedLocation.lat, this.selectedLocation.lng, newRadius);
      
      // Update the selected location with the new radius
      this.selectedLocation.radius = newRadius;
      this.selectedLocation.useMiles = this.useMiles;
      this.locationChange.emit(this.selectedLocation);
    }
  }

  /**
   * Centers the map on the user's current location
   */
  public centerOnUserLocation(): void {
    if (!this.isBrowser || !navigator.geolocation) {
      this.showError('Geolocation is not supported by your browser');
      return;
    }

    this.isLoading = true;
    this.errorMessage = null;
    
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const lat = position.coords.latitude;
        const lng = position.coords.longitude;
        this.centerMap(lat, lng, 'Your Location');
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      (error) => {
        this.isLoading = false;
        this.showError('Could not get your location. Please ensure location services are enabled.');
        this.cdr.detectChanges();
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 0
      }
    );
  }

  // Alias for template compatibility
  public get mapInitialized(): boolean {
    return this.isMapInitialized;
  }

  private showError(message: string, duration: number = 5000): void {
    this.errorMessage = message;
    this.cdr.detectChanges();

    // Auto-hide error after specified duration
    if (duration > 0) {
      setTimeout(() => {
        if (this.errorMessage === message) {
          this.errorMessage = null;
          this.cdr.detectChanges();
        }
      }, duration);
    }
  }

  private destroyMap(): void {
    if (this.map) {
      try {
        // Remove all map layers
        this.map.eachLayer(layer => {
          this.map?.removeLayer(layer);
        });

        // Remove the map instance
        this.map.remove();
      } catch (error) {
        console.error('Error destroying map:', error);
      } finally {
        this.map = null;
        this.marker = null;
        this.circle = null;
        this.isMapInitialized = false;
      }
    }
  }

  public centerMap(lat: number, lng: number, displayName?: string): void {
    if (!this.map) {
      console.warn('Map not initialized');
      return;
    }

    const location: L.LatLngExpression = [lat, lng];

    // Set view with smooth animation if zoom level is changing significantly
    const currentZoom = this.map.getZoom();
    const targetZoom = currentZoom < 10 ? 15 : currentZoom; // Don't zoom in too much if already zoomed

    this.map.flyTo(location, targetZoom, {
      duration: 1, // Animation duration in seconds
      easeLinearity: 0.25,
      animate: true
    });

    // Update or create marker
    this.updateMarkerPosition(lat, lng);

    // Update or create circle
    this.updateCircle(lat, lng, this.radius);

    // If displayName is provided, use it as the address
    // Otherwise, try to get the address via reverse geocoding
    if (displayName) {
      this.selectedLocation = { 
        lat, 
        lng, 
        latitude: lat, 
        longitude: lng,
        address: displayName, 
        displayName,
        radius: this.radius,
        useMiles: this.useMiles
      };
      this.locationChange.emit(this.selectedLocation);
    } else {
      // Try to get a friendly address for the coordinates
      this.reverseGeocode(lat, lng)
        .then(address => {
          this.selectedLocation = { 
            lat, 
            lng, 
            latitude: lat, 
            longitude: lng,
            address, 
            displayName: address,
            radius: this.radius,
            useMiles: this.useMiles
          };
          this.locationChange.emit(this.selectedLocation);
        })
        .catch(() => {
          // If reverse geocoding fails, just use coordinates
          const coords = `${lat.toFixed(4)}, ${lng.toFixed(4)}`;
          this.selectedLocation = {
            lat,
            lng,
            address: coords,
            displayName: 'Selected Location'
          };
          this.locationChange.emit(this.selectedLocation);
        });
    }
  }

  private updateMarkerPosition(lat: number, lng: number): void {
    if (!this.map) return;
    
    if (this.marker) {
      this.marker.setLatLng([lat, lng]);
      // Update draggable state if it has changed
      if (this.marker.dragging) {
        this.marker.dragging.disable();
      }
      if (this.draggableMarker) {
        this.marker.dragging?.enable();
      }
    } else {
      this.marker = L.marker([lat, lng], {
        draggable: this.draggableMarker,
        icon: iconDefault
      });
      
      if (this.draggableMarker) {
        this.marker.on('dragend', (e) => {
          const newLatLng = (e.target as L.Marker).getLatLng();
          this.updateLocation(newLatLng.lat, newLatLng.lng);
        });
      }
      
      this.marker.addTo(this.map);
    }
    
    if (!this.map.getBounds().contains([lat, lng])) {
      this.map.setView([lat, lng], this.map.getZoom());
    }
  }

  private updateCircle(lat: number, lng: number, radius: number): void {
    if (!this.map) return;
    
    const radiusInMeters = this.useMiles ? radius * 1609.34 : radius * 1000;

    if (this.circle) {
      this.circle.setLatLng([lat, lng]).setRadius(radiusInMeters);
    } else {
      this.circle = L.circle([lat, lng], {
        radius: radiusInMeters,
        color: '#4a6cf7',
        fillColor: '#4a6cf7',
        fillOpacity: 0.15,
        weight: 2,
        className: 'radius-circle'
      }).addTo(this.map);
    }
  }

  private async reverseGeocode(lat: number, lng: number): Promise<string> {
    try {
      const url = `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}&zoom=18&addressdetails=1`;

      const headers = new HttpHeaders({
        'User-Agent': 'BookSocialNetwork/1.0 (contact@example.com)'
      });

      const response = await firstValueFrom(
        this.http.get<any>(url, { headers })
      );

      if (response?.display_name) {
        return response.display_name;
      }

      // Fallback to a simple formatted address
      const addr = response?.address;
      if (addr) {
        return [
          addr.road,
          addr.neighbourhood,
          addr.suburb,
          addr.city,
          addr.county,
          addr.state,
          addr.country
        ].filter(Boolean).join(', ');
      }

      return `${lat.toFixed(4)}, ${lng.toFixed(4)}`;
    } catch (error) {
      console.error('Reverse geocoding error:', error);
      return `${lat.toFixed(4)}, ${lng.toFixed(4)}`;
    }
  }

  /**
   * Handles map click events
   * @param e - The Leaflet mouse event
   */
  private onMapClick(e: L.LeafletMouseEvent): void {
    if (this.isDragging || this.isLoading) return;
    
    const { lat, lng } = e.latlng;
    
    // Update the map UI
    this.centerMap(lat, lng);
    
    // Emit the location change immediately
    this.zone.run(() => {
      this.isLoading = true;
      this.reverseGeocode(lat, lng)
        .then(address => {
          this.updateLocation(lat, lng, address);
          this.isLoading = false;
          this.cdr.detectChanges();
        })
        .catch(() => {
          // If reverse geocoding fails, still update with coordinates
          this.updateLocation(lat, lng, `${lat.toFixed(4)}, ${lng.toFixed(4)}`);
          this.isLoading = false;
          this.cdr.detectChanges();
        });
    });
  }

  private updateLocation(lat: number, lng: number, address?: string): void {
    const location: LocationData = { 
      lat,
      lng,
      latitude: lat,
      longitude: lng,
      zoom: this.map?.getZoom(),
      address,
      displayName: address,
      radius: this.radius,
      useMiles: this.useMiles
    };
    this.selectedLocation = location;
    this.locationChange.emit(location);
    
    // Emit the location in the format expected by parent components
    // Ensure radius is always a number with a default value of 10 if not set
    const radius = typeof this.radius === 'number' ? this.radius : 10;
    this.locationSelected.emit({
      latitude: lat,
      longitude: lng,
      radius: radius
    });
  }

  private initializeCurrentLocation(): void {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const lat = position.coords.latitude;
          const lng = position.coords.longitude;
          this.centerMap(lat, lng, 'Your Location');
        },
        (error) => {
          console.error('Error getting location:', error);
          this.showError('Could not get your location. Using default position.');
          this.centerMap(this.initialLat, this.initialLng);
        }
      );
    } else {
      this.showError('Geolocation is not supported by this browser. Using default position.');
      this.centerMap(this.initialLat, this.initialLng);
    }
  }

  private setupResizeObserver(): void {
    if (typeof window === 'undefined' || !this.isBrowser || !this.mapContainerRef?.nativeElement) {
      return;
    }

    // Clean up any existing observer
    if (this.resizeObserver) {
      this.resizeObserver.disconnect();
      this.resizeObserver = null;
    }

    // Create new observer with proper type annotation
    const resizeObserver = new ResizeObserver((entries: ResizeObserverEntry[]) => {
      for (const entry of entries) {
        if (entry.target === this.mapContainerRef?.nativeElement && this.map) {
          // Small delay to ensure the container has finished resizing
          window.setTimeout(() => {
            this.map?.invalidateSize({ animate: false });
          }, 100);
        }
      }
    });

    // Store the observer reference
    this.resizeObserver = resizeObserver;

    // Start observing the map container
    const mapContainer = this.mapContainerRef?.nativeElement as HTMLElement;
    if (mapContainer) {
      try {
        resizeObserver.observe(mapContainer);
      } catch (error) {
        console.error('Error setting up ResizeObserver:', error);
      }
    }
  }

  private handleWindowResize(): void {
    if (this.resizeDebounceTimer) {
      window.clearTimeout(this.resizeDebounceTimer);
    }
    
    this.resizeDebounceTimer = window.setTimeout(() => {
      if (this.map) {
        this.map.invalidateSize({ animate: false });
      }
    }, 100);
  };

  /**
   * Handles location search using OpenStreetMap Nominatim
   */
  async onSearch(): Promise<void> {
    if (!this.searchQuery.trim() || !this.map) return;

    this.isSearching = true;
    this.errorMessage = null;
    this.cdr.detectChanges();

    try {
      const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(this.searchQuery)}&limit=1`;
      const headers = new HttpHeaders({
        'User-Agent': 'BookSocialNetwork/1.0 (contact@example.com)'
      });

      const response = await firstValueFrom(
        this.http.get<any[]>(url, { headers })
      );

      if (response && response.length > 0) {
        const { lat, lon, display_name } = response[0];
        const latitude = parseFloat(lat);
        const longitude = parseFloat(lon);
        
        // Center the map on the found location
        this.centerMap(latitude, longitude, display_name);
        
        // Update the selected location
        this.updateLocation(latitude, longitude, display_name);
      } else {
        this.showError('Location not found. Please try a different search term.');
      }
    } catch (error) {
      console.error('Search error:', error);
      this.showError('Error searching for location. Please try again later.');
    } finally {
      this.isSearching = false;
      this.cdr.detectChanges();
    }
  }

  /**
   * Clears the selected location and resets the map
   */
  public clearLocation(): void {
    this.selectedLocation = null;
    this.locationCleared.emit();
    // Emit undefined instead of null to match the expected type
    this.locationSelected.emit(undefined);
  }

  /**
   * Toggles between miles and kilometers for distance display
   */
  public toggleUnit(): void {
    this.useMiles = !this.useMiles;
  }

  /**
   * Angular lifecycle hook - Component initialization
   */
  ngOnInit(): void {
    if (this.isBrowser) {
      // Set up resize observer to handle map container resizing
      this.setupResizeObserver();
    }
  }

  /**
   * Initializes the map with default settings and sets up event listeners
   */
  private initializeMap(): void {
    if (!this.isBrowser || !this.mapContainerRef?.nativeElement) {
      console.warn('Map initialization skipped: Browser or container not available');
      return;
    }

    try {
      // Destroy any existing map instance
      this.destroyMap();

      // Create new map instance
      this.map = L.map(this.mapContainerRef.nativeElement, {
        center: [this.initialLat, this.initialLng],
        zoom: 13,
        minZoom: 5,
        maxZoom: 18,
        maxBounds: MOROCCO_BOUNDS,
        maxBoundsViscosity: 1.0,
        preferCanvas: true,
        renderer: L.canvas()
      });

      // Add OpenStreetMap tile layer
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; OpenStreetMap contributors',
        maxZoom: 19,
      }).addTo(this.map);

      // Set initial marker and circle
      this.updateMarkerPosition(this.initialLat, this.initialLng);
      this.updateCircle(this.initialLat, this.initialLng, this.initialRadius);

      // Set up map click handler
      this.map.on('click', (e: L.LeafletMouseEvent) => this.onMapClick(e));

      // Set up resize observer for the map container
      this.setupResizeObserver();

      this.isMapInitialized = true;
      this.cdr.detectChanges();
    } catch (error) {
      console.error('Error initializing map:', error);
      this.showError('Failed to initialize map. Please try again.');
    }
  }

  ngAfterViewInit(): void {
    if (!this.isBrowser) {
      return;
    }
    
    this.initializeMap();
    
    // Set up window resize handler
    const resizeHandler = this.handleWindowResize.bind(this);
    window.addEventListener('resize', resizeHandler);
    
    // Clean up event listeners on destroy
    this.cleanupFns.push(() => {
      window.removeEventListener('resize', resizeHandler);
    });
  }

  ngOnDestroy(): void {
    // Cleanup
    if (this.resizeObserver) {
      this.resizeObserver.disconnect();
      this.resizeObserver = null;
    }
    
    if (this.map) {
      this.map.remove();
      this.map = null;
    }
    
    if (this.isBrowser) {
      window.removeEventListener('resize', this.handleWindowResize);
    }
    
    this.isDestroyed = true;
    
    // Clear any pending debounce timers
    if (this.resizeDebounceTimer) {
      window.clearTimeout(this.resizeDebounceTimer);
      this.resizeDebounceTimer = null;
    }

    // Clean up the map and other resources
    this.destroyMap();
  }
}