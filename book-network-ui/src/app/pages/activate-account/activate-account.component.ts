import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthenticationControllerService } from '../../services/services';
import {CodeInputModule} from "angular-code-input";
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-activate-account',
  imports: [CodeInputModule,FormsModule,CommonModule],
  templateUrl: './activate-account.component.html',
  styleUrl: './activate-account.component.scss'
})
export class ActivateAccountComponent {
  message:string= '';
  isOkay:boolean= true;
  submitted:boolean= false;


  constructor(
    private router: Router,
    private authService: AuthenticationControllerService
  ){}

  onCodeCompleted(token:string):void{ //token hia l event li drna f html
    this.confirmaAccount(token)

  }
  private confirmaAccount(token: string):void {
    this.authService.confirm({
      token
    }).subscribe({
      next: ():void => {
        this.message = 'Your account has been successfully activated.\n Now you can proceed to login'
        this.submitted = true;
        this.isOkay = true;
      },
      error: () :void => {
        this.message = 'Token has been expired or invalid'
        this.submitted = true;
        this.isOkay = false;
      }
    });
  }


  redirectTologin():void{
    this.router.navigate(['login']);
  }
}
