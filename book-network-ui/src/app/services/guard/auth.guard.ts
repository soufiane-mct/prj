import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';

// Helper function to decode JWT and check expiration
function isTokenExpired(token: string): boolean {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    if (!payload.exp) return true;
    // exp is in seconds, Date.now() is in ms
    return Date.now() >= payload.exp * 1000;
  } catch (e) {
    return true;
  }
}

export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  
  // Check if we're in a browser environment
  if (typeof window !== 'undefined' && window.localStorage) {
    const token = localStorage.getItem('token');
    
    if (token && !isTokenExpired(token)) {
      return true;
    } else {
      localStorage.removeItem('token');
      router.navigate(['/login']);
      return false;
    }
  } else {
    // Server-side rendering - redirect to login
    router.navigate(['/login']);
    return false;
  }
};