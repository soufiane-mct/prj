import { Component, OnInit ,Inject, PLATFORM_ID} from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { TokenService } from '../../../../services/token/token.service';

@Component({
  selector: 'app-menu',
  imports: [RouterModule, CommonModule],
  templateUrl: './menu.component.html',
  styleUrl: './menu.component.scss'
})
export class MenuComponent implements OnInit {
  router: any;
  isAuthenticated = false;
  isAdmin = false;

  constructor(
    @Inject(PLATFORM_ID) private platformId: Object,
    private tokenService: TokenService
  ) {}

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      // Check authentication status
      this.checkAuthenticationStatus();
      
      const linkColor = document.querySelectorAll('.nav-link');
      linkColor.forEach(link => {
        if (window.location.href.endsWith(link.getAttribute('href') || '')) {
          link.classList.add('active');
        }
        link.addEventListener('click', () => {
          linkColor.forEach(l => l.classList.remove('active'));
          link.classList.add('active');
        });
      });
    }
  }

  checkAuthenticationStatus() {
    this.isAuthenticated = !!this.tokenService.token;
    this.isAdmin = false;
    if (this.isAuthenticated) {
      try {
        const token = this.tokenService.token;
        const payload = JSON.parse(atob(token.split('.')[1]));
        // Check for authorities/roles (adjust key as per your JWT)
        const roles = payload.roles || payload.authorities || [];
        if (Array.isArray(roles)) {
          this.isAdmin = roles.includes('ROLE_ADMIN') || roles.includes('ADMIN');
        } else if (typeof roles === 'string') {
          this.isAdmin = roles === 'ROLE_ADMIN' || roles === 'ADMIN';
        }
      } catch (e) {
        this.isAdmin = false;
      }
    }
  }

  logout() {
    localStorage.removeItem('token');
    window.location.href = '/login';
  }

  goToAdmin() {
    window.location.href = '/admin';
  }
}
