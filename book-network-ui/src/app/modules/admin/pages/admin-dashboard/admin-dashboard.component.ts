import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService } from '../../services/admin.service';


@Component({
  selector: 'app-admin-dashboard',
  imports: [CommonModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss']
})
export class AdminDashboardComponent implements OnInit {

  stats = {
    pendingApprovals: 0,
    totalUsers: 0,
    activeUsers: 0,
    blockedUsers: 0
  };

  pendingUsers: any[] = [];
  loading = true;
  error = '';

  constructor(private adminService: AdminService) { }

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.loading = true;
    this.error = '';

    // Load dashboard stats
    this.adminService.getDashboardStats().subscribe({
      next: (stats) => {
        this.stats = stats;
      },
      error: (error) => {
        console.error('Failed to load stats:', error);
        this.error = 'Failed to load dashboard statistics';
      }
    });

    // Load pending users
    this.adminService.getPendingUsers(0, 5).subscribe({
      next: (response) => {
        this.pendingUsers = response.content || [];
        this.loading = false;
      },
      error: (error) => {
        console.error('Failed to load pending users:', error);
        this.error = 'Failed to load pending users';
        this.loading = false;
      }
    });

    // Load total users count
    this.adminService.getAllUsers(0, 1).subscribe({
      next: (response) => {
        this.stats.totalUsers = response.totalElements || 0;
      },
      error: (error) => {
        console.error('Failed to load total users:', error);
      }
    });
  }

  approveUser(userId: number): void {
    this.adminService.approveUser(userId).subscribe({
      next: () => {
        alert('User approved successfully!');
        this.loadDashboardData();
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
          this.loadDashboardData();
        },
        error: (error) => {
          console.error('Failed to reject user:', error);
          alert('Failed to reject user. Please try again.');
        }
      });
    }
  }

  refreshData(): void {
    this.loadDashboardData();
  }

  exportUsers(): void {
    if (!this.pendingUsers || this.pendingUsers.length === 0) {
      alert('No users to export.');
      return;
    }
    const header = Object.keys(this.pendingUsers[0]).join(',');
    const rows = this.pendingUsers.map(user => Object.values(user).join(','));
    const csv = [header, ...rows].join('\n');
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'pending-users.csv';
    a.click();
    window.URL.revokeObjectURL(url);
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'ACTIVE':
        return 'badge-success';
      case 'PENDING':
        return 'badge-warning';
      case 'BLOCKED':
        return 'badge-danger';
      default:
        return 'badge-secondary';
    }
  }
}
