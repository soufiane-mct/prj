package com.aliboo.book.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User , Integer> {

    Optional<User> findByEmail(String email);
    
    // Admin queries for user management
    Page<User> findByEnabledTrueAndAccountLockedTrue(Pageable pageable);

long countByEnabledTrueAndAccountLockedTrue();
long countByEnabledTrueAndAccountLockedFalse();
long countByEnabledFalseAndAccountLockedTrue();
    
    @Query("SELECT u FROM User u WHERE u.enabled = true AND u.accountLocked = false")
    Page<User> findApprovedUsers(Pageable pageable);
}
