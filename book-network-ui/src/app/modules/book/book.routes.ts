import { Routes } from '@angular/router';
import { MainComponent } from './pages/main/main.component';
import { BookListComponent } from './pages/book-list/book-list.component';
import { MyBooksComponent } from './pages/my-books/my-books.component';
import { ManageBookComponent } from './pages/manage-book/manage-book.component';
import { BorrowedBookListComponent } from './pages/borrowed-book-list/borrowed-book-list.component';
import { ReturnBooksComponent } from './pages/return-books/return-books.component';
import { BookDetailsComponent } from './pages/book-details/book-details.component';
import { GuestRentRequestsComponent } from './pages/guest-rent-requests/guest-rent-requests.component';
import { authGuard } from '../../services/guard/auth.guard';

export const BOOK_ROUTES: Routes = [
  {
    path: '',
    component: MainComponent,
    children: [
      { path: '', component: BookListComponent },
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
        path: 'details/:bookId', 
        component: BookDetailsComponent 
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
        path: 'guest-rent-requests', 
        component: GuestRentRequestsComponent,
        canActivate: [authGuard]
      },
      // Fallback route
      { path: '**', redirectTo: '' }
    ]
  }
];
