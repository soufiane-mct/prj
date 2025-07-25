package com.aliboo.book.admin;

import com.aliboo.book.user.User;
import com.aliboo.book.user.UserRepository;
import com.aliboo.book.user.TokenRepository;
import com.aliboo.book.book.BookRepository;
import jakarta.persistence.EntityNotFoundException;

import com.aliboo.book.history.BookTransactionHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {
    
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final BookRepository bookRepository;
    private final BookTransactionHistoryRepository bookTransactionHistoryRepository;
    private final com.aliboo.book.book.GuestRentRequestRepository guestRentRequestRepository;
    private final com.aliboo.book.feedback.FeedbackRepository feedbackRepository;
    
    /**
     * Get all pending users (enabled but account locked - waiting for admin approval)
     */
    public Page<User> getPendingUsers(Pageable pageable) {
        return userRepository.findByEnabledTrueAndAccountLockedTrue(pageable);
    }
    
    /**
     * Get all users with pagination
     */
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
    
    /**
     * Approve a user - unlock their account so they can login
     */
    public void approveUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        user.setAccountLocked(false);
        userRepository.save(user);
    }
    
    /**
     * Reject/Lock a user account
     */
    public void rejectUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        user.setAccountLocked(true);
        userRepository.save(user);
    }
    
    /**
     * Delete a user account completely
     */
    @Transactional
public void deleteUser(Integer userId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

    // 1. Retrieve all books owned by the user
    java.util.List<com.aliboo.book.book.Book> books = bookRepository.findByOwner_Id(userId);

    // 2. For each book, delete dependent entities
    for (com.aliboo.book.book.Book book : books) {
        // Delete all guest rent requests referencing the book
        guestRentRequestRepository.deleteAllByBookId(book.getId());
        // Delete all feedback entries referencing the book
        feedbackRepository.deleteAllByBookId(book.getId());
        // Delete all transaction histories for this book
        bookTransactionHistoryRepository.deleteAllByBookId(book.getId());
        // Clear owner reference before deleting books
        book.setOwner(null);
        bookRepository.save(book);
    }

    // 3. Delete all tokens for this user
    tokenRepository.deleteAllByUserId(userId);
    // 4. Delete all books owned by the user
    bookRepository.deleteAllByOwnerId(userId);
    // 5. Delete the user
    userRepository.delete(user);
}

    /**
     * Force delete a user and all their books
     */
    public void forceDeleteUserAndBooks(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
        // Delete all books owned by the user
        bookRepository.deleteAllByOwnerId(userId);
        // Delete all transaction history for this user
        bookTransactionHistoryRepository.deleteAllByUserId(userId);
        // Delete all tokens for this user
        tokenRepository.deleteAllByUserId(userId);
        // Delete the user
        userRepository.delete(user);
    }
    
    /**
     * Get user details by ID
     */
    public User getUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
    }
    
    /**
     * Get count of pending approvals
     */
    public long getPendingApprovalsCount() {
        return userRepository.countByEnabledTrueAndAccountLockedTrue();
    }

    public long getTotalUsersCount() {
        return userRepository.count();
    }

    public long getActiveUsersCount() {
        return userRepository.countByEnabledTrueAndAccountLockedFalse();
    }

    public long getBlockedUsersCount() {
        return userRepository.countByEnabledFalseAndAccountLockedTrue();
    }
}
