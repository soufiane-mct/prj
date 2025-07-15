import { Component } from '@angular/core';
import { MenuComponent } from "../../components/menu/menu.component";
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { TokenService } from '../../../../services/token/token.service';

@Component({
  selector: 'app-main',
  imports: [MenuComponent, RouterModule, CommonModule],
  templateUrl: './main.component.html',
  styleUrl: './main.component.scss'
})
export class MainComponent {
  isAuthenticated = false;

  constructor(private tokenService: TokenService) {
    this.checkAuthenticationStatus();
  }

  checkAuthenticationStatus() {
    this.isAuthenticated = !!this.tokenService.token;
  }
}
