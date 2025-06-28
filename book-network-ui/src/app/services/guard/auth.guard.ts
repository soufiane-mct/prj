import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';

export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  
  // Check if we're in a browser environment
  if (typeof window !== 'undefined' && window.localStorage) {
    const token = localStorage.getItem('token');
    
    if (token) {
      return true;
    } else {
      router.navigate(['/login']);
      return false;
    }
  } else {
    // Server-side rendering - redirect to login
    router.navigate(['/login']);
    return false;
  }
};