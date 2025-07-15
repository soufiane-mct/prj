import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MainComponent } from './pages/main/main.component';
import { BookListComponent } from './pages/book-list/book-list.component';
import { MyBooksComponent } from './pages/my-books/my-books.component';
import { ManageBookComponent } from './pages/manage-book/manage-book.component';
import { BorrowedBookListComponent } from './pages/borrowed-book-list/borrowed-book-list.component';
import { ReturnBooksComponent } from './pages/return-books/return-books.component';
import { BookDetailsComponent } from './pages/book-details/book-details.component';
import { authGuard } from '../../services/guard/auth.guard';

const routes: Routes = [
  {
    path: '',
    component: MainComponent,
    children: [
      {
        path: '',
        component: BookListComponent //dkhl jibha mn pages
      },
      {
        path: 'my-products',
        component: MyBooksComponent,
        canActivate: [authGuard]
      },
      {
        path: 'my-borrowed-products',
        component: BorrowedBookListComponent,
        canActivate: [authGuard]
      },
      {
        path: 'my-returned-products',
        component: ReturnBooksComponent,
        canActivate: [authGuard]
      },
      {
        path: 'guest-rent-requests',
        loadComponent: () => import('./pages/guest-rent-requests/guest-rent-requests.component').then(m => m.GuestRentRequestsComponent),
        canActivate: [authGuard]
      },
      {
        path: 'details/:bookId',
        component: BookDetailsComponent,
      },
      {
        path: 'manage',
        component: ManageBookComponent,
        canActivate: [authGuard]
      },
      {
        path: 'manage/:bookId',
        component: ManageBookComponent,
        canActivate: [authGuard]
      },
      {
        path: 'categories',
        loadComponent: () => import('./pages/category-management/category-management.component').then(m => m.CategoryManagementComponent),
        canActivate: [authGuard]
      }

    ]
  }
  ];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BookRoutingModule { }
