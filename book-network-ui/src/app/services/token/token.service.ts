import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

@Injectable({
  providedIn: 'root'
})
export class TokenService {
  constructor(@Inject(PLATFORM_ID) private platformId: Object) {}

  set token(token: string) {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem('token', token);
    }
  }

  get token(): string {
    if (isPlatformBrowser(this.platformId)) {
      return localStorage.getItem('token') as string;
    }
    return '';
  }

  isLogged(): boolean {
    return !!this.token;
  }

  getRole(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      const token = this.token;
      if (!token) return null;
      
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        return payload.role || null;
      } catch (e) {
        console.error('Error decoding token', e);
        return null;
      }
    }
    return null;
  }

  clearToken(): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem('token');
    }
  }
}
