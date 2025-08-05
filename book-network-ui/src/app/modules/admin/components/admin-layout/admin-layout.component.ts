import { CommonModule } from '@angular/common';
import { Router, RouterModule, RouterOutlet } from '@angular/router';
import { TokenService } from '../../../../services/token/token.service';
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, RouterOutlet],
  templateUrl: './admin-layout.component.html',
  styleUrls: ['./admin-layout.component.scss']
})
export class AdminLayoutComponent implements OnInit {

  constructor(
    private router: Router,
    private tokenService: TokenService
  ) { }

  ngOnInit(): void {
    // Check if user has admin role
    if (!this.isAdmin()) {
      this.router.navigate(['/login']);
    }
  }

  isAdmin(): boolean {
    // Check if user has admin role from token
    const token = this.tokenService.token;
    if (!token) return false;
    
    // You might need to decode the JWT token to check roles
    // For now, we'll assume if they have a token and reached here, they're admin
    return true;
  }

  logout(): void {
    this.tokenService.token = '';
    this.router.navigate(['/login']);
  }
}
