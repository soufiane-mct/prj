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
  }

  logout() {
    localStorage.removeItem('token');
    window.location.href = '/login';
  }
}
