import { Component } from '@angular/core';
import { RegistrationRequest } from '../../services/models';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthenticationControllerService } from '../../services/services';

@Component({
  selector: 'app-register',
  imports: [FormsModule,CommonModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})

export class RegisterComponent {

  registerRequest: RegistrationRequest= {email: '', firstname: '',lastname:'',password:''} ;
  errMsg: Array<string> = [];

  constructor(
    private router: Router,
    private authService: AuthenticationControllerService //l auth dl openapi
  ){

  }

//   register():void {
//     this.errMsg = [];
//     this.authService.register({
//       body: this.registerRequest //dkhl lia data mn body diha l registerRequest
//     }).subscribe({
//       next: () => {
//         this.router.navigate(['activate-account']) //la t9iyd succusfuly dih l activated-account page
//       },
//       error: (err) => {
//        this.errMsg = err.error.validationErrors;
//        //this.errMsg = err.error?.validationErrors || ['An unexpected error occurred.'];
//
//       }
//     })
//
//   }
register(): void {
  this.errMsg = [];

  this.authService.register({
    body: this.registerRequest
  }).subscribe({
    next: () => {
      this.router.navigate(['activate-account']);
    },
    error: async (err) => {
      console.log('Full register error:', err);

      if (err.error instanceof Blob) {
        const errorText = await err.error.text();
        try {
          const parsed = JSON.parse(errorText);
          console.log('Parsed register error:', parsed);

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
          console.error('Register JSON parse error:', e);
          this.errMsg.push('Could not parse register error response.');
        }
      } else {
        this.errMsg.push('Unexpected register error type.');
      }
    }
  });
}


  login():void {
    this.router.navigate(['login']); //fsh y click ela login ydih l login page

  }

}
