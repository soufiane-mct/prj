package com.aliboo.book.admin;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminDashboardStats {
    private long pendingApprovals;
    private long totalUsers;
    private long activeUsers;
    private long blockedUsers;
}
