import { ApplicationConfig } from '@angular/core';
import { provideRouter, withInMemoryScrolling } from '@angular/router';
import { routes } from './app.routes';
import { provideHttpClient, withInterceptorsFromDi, withFetch } from '@angular/common/http';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { provideZoneChangeDetection } from '@angular/core';
import { HttpTokenInterceptor } from './services/interceptor/http-token.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ 
      eventCoalescing: true,
      runCoalescing: true
    }),
    provideRouter(
      routes,
      withInMemoryScrolling({
        scrollPositionRestoration: 'enabled',
        anchorScrolling: 'enabled'
      })
    ),
    provideHttpClient(
      withInterceptorsFromDi(),
      withFetch()
    ),
    { 
      provide: HTTP_INTERCEPTORS, 
      useClass: HttpTokenInterceptor, 
      multi: true 
    }
  ]
};
