package com.aliboo.book.book;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GuestRentRequestRepository extends JpaRepository<GuestRentRequest, Integer> {
    List<GuestRentRequest> findByBook_Owner_Id(Integer ownerId);
} 