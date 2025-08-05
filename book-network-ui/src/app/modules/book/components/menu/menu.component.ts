import { Component, OnInit, HostListener, Inject, PLATFORM_ID, ElementRef, Renderer2 } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { TokenService } from '../../../../services/token/token.service';

@Component({
  selector: 'app-menu',
  imports: [RouterModule, CommonModule],
  templateUrl: './menu.component.html',
  styleUrl: './menu.component.scss'
})
export class MenuComponent implements OnInit {

  isAuthenticated = false;
  isAdmin = false;
  private lastScroll = 0;
  private navbar: HTMLElement | null = null;
  private readonly SCROLL_THRESHOLD = 5; // Minimum scroll amount to trigger hide/show
  private readonly NAVBAR_HEIGHT = 56; // Height of your navbar in pixels

  constructor(
    @Inject(PLATFORM_ID) private platformId: Object,
    private tokenService: TokenService,
    private el: ElementRef,
    private renderer: Renderer2,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      // Initialize navbar reference
      this.navbar = this.el.nativeElement.querySelector('.navbar');
      
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

  @HostListener('window:scroll', [])
  onWindowScroll() {
    if (!isPlatformBrowser(this.platformId) || !this.navbar) return;

    const currentScroll = window.pageYOffset;
    
    // Only trigger if scrolled more than the threshold
    if (Math.abs(currentScroll - this.lastScroll) < this.SCROLL_THRESHOLD) return;

    if (currentScroll > this.lastScroll && currentScroll > this.NAVBAR_HEIGHT) {
      // Scrolling down
      this.renderer.addClass(this.navbar, 'navbar-hide');
      this.renderer.removeClass(this.navbar, 'navbar-show');
    } else {
      // Scrolling up or at top of page
      this.renderer.addClass(this.navbar, 'navbar-show');
      this.renderer.removeClass(this.navbar, 'navbar-hide');
    }
    
    this.lastScroll = currentScroll;
  }

  // Reset navbar state when menu is toggled (for mobile)
  onMenuToggle() {
    if (this.navbar) {
      this.renderer.removeClass(this.navbar, 'navbar-hide');
      this.renderer.addClass(this.navbar, 'navbar-show');
    }
  }

  reloadPage(event: Event) {
    event.preventDefault();
    
    // Always scroll to top first
    window.scrollTo(0, 0);
    
    // If already on products page, just reload
    if (this.router.url.includes('/products')) {
      // Force a hard reload to ensure we get fresh data
      window.location.href = '/products';
    } else {
      // Navigate to products page
      this.router.navigate(['/products']).then(() => {
        // After navigation, force a hard reload to ensure fresh data
        window.location.reload();
      }).catch(() => {
        // If navigation fails, try direct URL navigation
        window.location.href = '/products';
      });
    }
  }
}
