// import { Component } from '@angular/core';
// import { FormsModule } from '@angular/forms';
// import { CommonModule } from '@angular/common';
// @Component({
//   selector: 'app-login',
//   imports: [FormsModule,CommonModule],
//   templateUrl: './login.component.html',
//   styleUrl: './login.component.scss'
// })
// export class LoginComponent {
//   authRequest: AuthenticationRequest = {email: '', password: ''}; //authRequest atakhd l 9iyam li aydkhl l user
//   errMsg: Array<string> = [];//ta hna la tl3 err atakhdhom
//
// }
//
// login(): void {
//
//   }
//
// register(): void {
//
//   }

import { Router } from '@angular/router';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { error } from 'node:console';
import { AuthenticationControllerService } from '../../services/services';
import { TokenService } from '../../services/token/token.service';
export interface AuthenticationRequest {
  email: string;
  password: string;
}

@Component({
  selector: 'app-login',
  imports: [FormsModule,CommonModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  authRequest: AuthenticationRequest = {email: '', password: ''}; //authRequest atakhd l 9iyam li aydkhl l user
  errMsg: Array<string> = [];//ta hna la tl3 err atakhdhom

constructor(
  private router: Router,
  private authService: AuthenticationControllerService, // AuthenticationControllerService hia l auth li jbnaha mn spring
  private tokenService: TokenService // drnaha f ../../services/token/token.service
  ) {}

  login(): void { //hna fsh user aybrk ela login
    this.errMsg = [];//lkn email o pass khlt dir lia errmsg li drna

    this.authService.authenticate({
      body: this.authRequest
    }).subscribe({
      next: (res): void =>{
        // console.log('Login response:', res);
        this.tokenService.token = res.token as string;

        //save the token
        this.router.navigate(['books']);
      },//dir authenticate o save l token lkn howa o moraha dih l books page

      error: async (err): Promise<void> => {//khdmna b Blob hna bsh yhwl data li drna fl backend l json o y9raha lina
        console.log('Full error:', err);

        if (err.error instanceof Blob) {
          const errorText = await err.error.text(); // read the Blob content as string
          try {
            const parsed = JSON.parse(errorText); // parse it to JSON
            console.log('Parsed error:', parsed);

            if (Array.isArray(parsed)) {
              this.errMsg = parsed;
            } else if (parsed.validationErrors) {
              this.errMsg = parsed.validationErrors;
            } else if (parsed.errMsg) {
              this.errMsg.push(parsed.errMsg);
            } else {
              this.errMsg.push('Unexpected error format.');
            }
          } catch (e) {
            console.error('JSON parse error:', e);
            this.errMsg.push('Could not parse error response.');
          }
        } else {
           this.errMsg.push('Unexpected error type.');
          //this.errMsg.push(err.error.error);

        }
      }
    });
  }

  register(): void { //hna fsh user aybrk ela register andiwh l page d register
    this.router.navigate(['register'])
  }
}
