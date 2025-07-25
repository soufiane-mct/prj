package com.aliboo.book.admin;

import com.aliboo.book.user.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin")
@PreAuthorize("hasRole('ADMIN')") // Only admins can access these endpoints
public class AdminController {
    
    private final AdminService adminService;
    
    @GetMapping("/users/pending")
    public ResponseEntity<Page<UserResponse>> getPendingUsers(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<User> users = adminService.getPendingUsers(pageable);
        Page<UserResponse> userResponses = users.map(this::mapToUserResponse);
        
        return ResponseEntity.ok(userResponses);
    }
    
    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<User> users = adminService.getAllUsers(pageable);
        Page<UserResponse> userResponses = users.map(this::mapToUserResponse);
        
        return ResponseEntity.ok(userResponses);
    }
    
    @PostMapping("/users/{user-id}/approve")
    public ResponseEntity<Void> approveUser(@PathVariable("user-id") Integer userId) {
        adminService.approveUser(userId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/users/{user-id}/reject")
    public ResponseEntity<Void> rejectUser(@PathVariable("user-id") Integer userId) {
        adminService.rejectUser(userId);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/users/{user-id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("user-id") Integer userId) {
        try {
            adminService.deleteUser(userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException ex) {
            if ("USER_OWNS_BOOKS".equals(ex.getMessage())) {
                // 409 Conflict: user owns books, admin must confirm
                return ResponseEntity.status(409).build();
            }
            throw ex;
        }
    }

    @DeleteMapping("/users/{user-id}/force")
    public ResponseEntity<Void> forceDeleteUser(@PathVariable("user-id") Integer userId) {
        adminService.forceDeleteUserAndBooks(userId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/users/{user-id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable("user-id") Integer userId) {
        User user = adminService.getUserById(userId);
        return ResponseEntity.ok(mapToUserResponse(user));
    }
    
    @GetMapping("/dashboard/stats")
    public ResponseEntity<AdminDashboardStats> getDashboardStats() {
        long pendingCount = adminService.getPendingApprovalsCount();
        long totalUsers = adminService.getTotalUsersCount();
        long activeUsers = adminService.getActiveUsersCount();
        long blockedUsers = adminService.getBlockedUsersCount();

        AdminDashboardStats stats = AdminDashboardStats.builder()
                .pendingApprovals(pendingCount)
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .blockedUsers(blockedUsers)
                .build();

        return ResponseEntity.ok(stats);
    }
    
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .accountLocked(user.isAccountLocked())
                .enabled(user.isEnabled())
                .createdDate(user.getCreatedDate())
                .lastModifiedDate(user.getLastModifiedDate())
                .build();
    }
}
