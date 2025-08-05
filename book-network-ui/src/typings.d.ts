// This file contains type declarations for Leaflet

declare global {
  interface Window {
    L: typeof import('leaflet');
  }
}

declare namespace L {
  export interface LatLng {
    lat: number;
    lng: number;
    toArray(): [number, number];
    equals(otherLatLng: LatLng): boolean;
    distanceTo(otherLatLng: LatLng): number;
  }

  export interface Map {
    setView(center: [number, number] | LatLng, zoom: number, options?: any): this;
    on(event: string, handler: (e: any) => void): this;
    off(event: string, handler?: (e: any) => void): this;
    remove(): void;
    invalidateSize(animate?: boolean): this;
    getCenter(): LatLng;
    getZoom(): number;
    panTo(latlng: [number, number] | LatLng, options?: any): this;
  }

  export interface Marker {
    setLatLng(latlng: [number, number] | LatLng): this;
    getLatLng(): LatLng;
    addTo(map: Map): this;
    remove(): void;
    on(event: string, handler: (e: any) => void): this;
    off(event: string, handler?: (e: any) => void): this;
  }

  export interface Circle {
    setLatLng(latlng: [number, number] | LatLng): this;
    setRadius(radius: number): this;
    getLatLng(): LatLng;
    getRadius(): number;
    addTo(map: Map): this;
    remove(): void;
  }

  export interface TileLayer {
    addTo(map: Map): this;
  }

  export interface LeafletMouseEvent {
    latlng: LatLng;
    layerPoint: any;
    containerPoint: any;
    originalEvent: MouseEvent;
  }
}

declare const L: {
  map(container: string | HTMLElement, options?: any): L.Map;
  latLng(latitude: number, longitude: number): L.LatLng;
  marker(latlng: [number, number] | L.LatLng, options?: any): L.Marker;
  circle(latlng: [number, number] | L.LatLng, options?: any): L.Circle;
  tileLayer(urlTemplate: string, options?: any): L.TileLayer;
  icon(options: any): any;
  control: any;
  DomUtil: any;
  DomEvent: any;
  Util: any;
  extend<T>(dest: T, ...sources: any[]): T;
  bind(fn: Function, ...obj: any[]): Function;
  stamp(obj: any): number;
  setOptions(obj: any, options: any): any;
  get(id: string | number | HTMLElement): any;
  $: (id: string | HTMLElement) => HTMLElement | null;
  Browser: any;
  Point: any;
  Bounds: any;
  LineUtil: any;
  PolyUtil: any;
  Evented: any;
  Class: any;
  Handler: any;
  Mixin: any;
  Layer: any;
  LayerGroup: any;
  FeatureGroup: any;
  Popup: any;
  Tooltip: any;
  Control: any;
  Projection: any;
  CRS: any;
  GeoJSON: any;
  LatLngBounds: any;
  LatLng: any;
  Point: any;
  Bounds: any;
  Browser: any;
  Util: any;
  extend(dest: any, ...src: any[]): any;
  bind(fn: Function, ...obj: any[]): Function;
  stamp(obj: any): number;
  setOptions(obj: any, options: any): any;
  get(id: string | number | HTMLElement): any;
  $: (id: string | HTMLElement) => HTMLElement | null;
  noop(): void;
  falseFn(): boolean;
  formatNum(num: number, digits?: number): number;
  trim(str: string): string;
  splitWords(str: string): string[];
  setOptions(obj: any, options: any): any;
  getParamString(obj: any, existingUrl?: string, uppercase?: boolean): string;
  template(str: string, data: any): string;
  isArray(obj: any): boolean;
  indexOf(array: any[], el: any): number;
  requestAnimFrame(fn: Function, context?: any, immediate?: boolean): number;
  cancelAnimFrame(id: number): void;
  bind(fn: Function, ...obj: any[]): Function;
  stamp(obj: any): number;
  throttle(fn: Function, time: number, context: any): Function;
  wrapNum(num: number, range: number[], includeMax?: boolean): number;
  falseFn(): boolean;
  formatNum(num: number, digits?: number): number;
  trim(str: string): string;
  splitWords(str: string): string[];
  setOptions(obj: any, options: any): any;
  getParamString(obj: any, existingUrl?: string, uppercase?: boolean): string;
  template(str: string, data: any): string;
  isArray(obj: any): boolean;
  indexOf(array: any[], el: any): number;
  requestAnimFrame(fn: Function, context?: any, immediate?: boolean): number;
  cancelAnimFrame(id: number): void;
  bind(fn: Function, ...obj: any[]): Function;
  stamp(obj: any): number;
  throttle(fn: Function, time: number, context: any): Function;
  wrapNum(num: number, range: number[], includeMax?: boolean): number;
};
