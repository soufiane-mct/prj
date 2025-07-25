import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AdminService {

  private readonly baseUrl = 'http://localhost:8081/api/v1/admin';

  constructor(private http: HttpClient) { }

  // Dashboard stats
  getDashboardStats(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/dashboard/stats`);
  }

  // Get all users with pagination
  getAllUsers(page: number = 0, size: number = 10): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<any>(`${this.baseUrl}/users`, { params });
  }

  // Get pending users with pagination
  getPendingUsers(page: number = 0, size: number = 10): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<any>(`${this.baseUrl}/users/pending`, { params });
  }

  // Get user by ID
  getUserById(userId: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/users/${userId}`);
  }

  // Approve user
  approveUser(userId: number): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/users/${userId}/approve`, {});
  }

  // Reject/Block user
  rejectUser(userId: number): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/users/${userId}/reject`, {});
  }

  // Delete user
  deleteUser(userId: number): Observable<any> {
    return this.http.delete<any>(`${this.baseUrl}/users/${userId}`);
  }
}
