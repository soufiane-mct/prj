import { Component } from '@angular/core';
import { TokenService } from '../../../../services/token/token.service';
import { MenuComponent } from '../../components/menu/menu.component';
import { RouterModule, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-main',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    RouterOutlet,
    MenuComponent
  ],
  templateUrl: './main.component.html',
  styleUrls: ['./main.component.scss']
})
export class MainComponent {
  isAuthenticated = false;

  constructor(private tokenService: TokenService) {
    this.checkAuthenticationStatus();
  }

  private checkAuthenticationStatus(): void {
    this.isAuthenticated = !!this.tokenService.token;
  }
}
