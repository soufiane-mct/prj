import { Routes } from '@angular/router';
import { authGuard } from './services/guard/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () => import('./pages/register/register.component').then(m => m.RegisterComponent)
  },
  {
    path: 'activate-account',
    loadComponent: () => import('./pages/activate-account/activate-account.component').then(m => m.ActivateAccountComponent)
  },
  {
    path: 'products',
    loadChildren: () => import('./modules/book/book.routes').then(m => m.BOOK_ROUTES)
  },
  {
    path: 'admin',
    loadChildren: () => import('./modules/admin/admin.routes').then(m => m.ADMIN_ROUTES),
    canActivate: [authGuard]
  },
  {
    path: '',
    redirectTo: 'products',
    pathMatch: 'full'
  },
  {
    path: '**',
    redirectTo: 'products'
  }
];
