import { TestBed } from '@angular/core/testing';
import { HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { HttpTokenInterceptor } from './http-token.interceptor';
import { TokenService } from '../token/token.service';
import { of } from 'rxjs';

describe('HttpTokenInterceptor', () => {
  let interceptor: HttpTokenInterceptor;
  let tokenServiceSpy: jasmine.SpyObj<TokenService>;

  beforeEach(() => {
    tokenServiceSpy = jasmine.createSpyObj('TokenService', [], { token: 'test-token' });

    TestBed.configureTestingModule({
      providers: [
        HttpTokenInterceptor,
        { provide: TokenService, useValue: tokenServiceSpy }
      ]
    });

    interceptor = TestBed.inject(HttpTokenInterceptor);
  });

  it('should be created', () => {
    expect(interceptor).toBeTruthy();
  });

  it('should add Authorization header if token exists', () => {
    const httpRequest = new HttpRequest('GET', '/test');
    const httpHandler: HttpHandler = {
      handle: (req: HttpRequest<any>) => {
        expect(req.headers.get('Authorization')).toBe('Bearer test-token');
        return of({} as HttpEvent<any>);
      }
    };
    interceptor.intercept(httpRequest, httpHandler).subscribe();
  });

  it('should not add Authorization header if token does not exist', () => {
    tokenServiceSpy.token = '';
    const httpRequest = new HttpRequest('GET', '/test');
    const httpHandler: HttpHandler = {
      handle: (req: HttpRequest<any>) => {
        expect(req.headers.has('Authorization')).toBeFalse();
        return of({} as HttpEvent<any>);
      }
    };
    interceptor.intercept(httpRequest, httpHandler).subscribe();
  });
});