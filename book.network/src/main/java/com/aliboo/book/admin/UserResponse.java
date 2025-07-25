package com.aliboo.book.admin;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    private Integer id;
    private String firstname;
    private String lastname;
    private String email;
    private boolean accountLocked;
    private boolean enabled;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    
    public String getFullName() {
        return firstname + " " + lastname;
    }
    
    public String getStatus() {
        if (!enabled) {
            return "Email Not Verified";
        } else if (accountLocked) {
            return "Pending Admin Approval";
        } else {
            return "Active";
        }
    }
}
