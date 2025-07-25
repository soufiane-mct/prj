import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

import { AdminService } from '../../services/admin.service';

@Component({
  selector: 'app-admin-users',
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-users.component.html',
  styleUrls: ['./admin-users.component.scss']
})
export class AdminUsersComponent implements OnInit {

  users: any[] = [];
  loading = true;
  error = '';
  
  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;
  
  // Filters
  statusFilter = '';
  searchQuery = '';

  constructor(private adminService: AdminService) { }

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.error = '';

    let request;
    if (this.statusFilter === 'pending') {
      request = this.adminService.getPendingUsers(this.currentPage, this.pageSize);
    } else {
      request = this.adminService.getAllUsers(this.currentPage, this.pageSize);
    }

    request.subscribe({
      next: (response) => {
        this.users = response.content || [];
        this.totalPages = response.totalPages || 0;
        this.totalElements = response.totalElements || 0;
        this.loading = false;
      },
      error: (error) => {
        console.error('Failed to load users:', error);
        this.error = 'Failed to load users. Please try again.';
        this.loading = false;
      }
    });
  }

  approveUser(userId: number): void {
    this.adminService.approveUser(userId).subscribe({
      next: () => {
        alert('User approved successfully!');
        this.loadUsers();
      },
      error: (error) => {
        console.error('Failed to approve user:', error);
        alert('Failed to approve user. Please try again.');
      }
    });
  }

  rejectUser(userId: number): void {
    if (confirm('Are you sure you want to reject this user?')) {
      this.adminService.rejectUser(userId).subscribe({
        next: () => {
          alert('User rejected successfully!');
          this.loadUsers();
        },
        error: (error) => {
          console.error('Failed to reject user:', error);
          alert('Failed to reject user. Please try again.');
        }
      });
    }
  }

  deleteUser(userId: number): void {
    if (confirm('Are you sure you want to permanently delete this user? This action cannot be undone.')) {
      this.adminService.deleteUser(userId).subscribe({
        next: () => {
          alert('User deleted successfully!');
          this.loadUsers();
        },
        error: (error) => {
          console.error('Failed to delete user:', error);
          alert('Failed to delete user. Please try again.');
        }
      });
    }
  }

  onFilterChange(): void {
    this.currentPage = 0;
    this.loadUsers();
  }

  onSearchChange(): void {
    this.currentPage = 0;
    this.loadUsers();
  }

  onPageSizeChange(): void {
    this.currentPage = 0;
    this.loadUsers();
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadUsers();
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadUsers();
    }
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'Active': return 'status-active';
      case 'Pending Admin Approval': return 'status-pending';
      case 'Email Not Verified': return 'status-unverified';
      default: return 'status-blocked';
    }
  }

  getUserActions(user: any): string[] {
    const actions = [];
    
    if (user.accountLocked && user.enabled) {
      actions.push('approve');
    }
    
    if (!user.accountLocked && user.enabled) {
      actions.push('reject');
    }
    
    actions.push('delete');
    
    return actions;
  }

  refreshUsers(): void {
    this.loadUsers();
  }

  exportUsers(): void {
    alert('Export functionality coming soon!');
  }
}
