package com.aliboo.book.book;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GuestRentRequestRepository extends JpaRepository<GuestRentRequest, Integer> {
    List<GuestRentRequest> findByBook_Owner_Id(Integer ownerId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query("DELETE FROM GuestRentRequest g WHERE g.book.id = :bookId")
    void deleteAllByBookId(Integer bookId);
} 