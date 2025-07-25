import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login.component'; //hna adir l import dyal l component li bghiti tdir lih routing ltht
import { RegisterComponent } from './pages/register/register.component';
import { ActivateAccountComponent } from './pages/activate-account/activate-account.component';
import { authGuard } from './services/guard/auth.guard';

export const routes: Routes = [
    {
      path: 'login',
      component: LoginComponent
    }, //hna adir l path li atkhdm fih lclass LoginComponent

    {
      path: 'register',
      component: RegisterComponent
    },
    {
      path: 'activate-account',
      component: ActivateAccountComponent
    },
    {
     path: 'products',
     loadChildren: () => import('./modules/book/book.module').then(m => m.BookModule) //module.BookModule hit shild path dl products
    },
    {
      path: 'admin',
      loadChildren: () => import('./modules/admin/admin.module').then(m => m.AdminModule),
      canActivate: [authGuard] // Only authenticated admins can access
    },
    {
      path: '',
      redirectTo: '/products',
      pathMatch: 'full'
    }

  ];
