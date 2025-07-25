package com.aliboo.book.book;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

//import java.awt.print.Pageable;
import org.springframework.data.domain.Pageable;

public interface BookRepository extends JpaRepository<Book, Integer>, JpaSpecificationExecutor<Book> { //tania JpaSpecificationExecutor khdmna biha fl findAllBooksByOwner fl bookservice

    @Modifying
    @Transactional
    @Query("DELETE FROM Book b WHERE b.owner.id = :ownerId")
    void deleteAllByOwnerId(Integer ownerId);



    @Query("""
            SELECT book
            FROM Book book
            WHERE book.archived = false
            AND book.shareable = true
            AND book.owner.id != :userId
            """)
    Page<Book> findAllDisplayableBooks(Pageable pageable, Integer userId);

    // Retrieve all books by owner id
    java.util.List<Book> findByOwner_Id(Integer ownerId);

    @Query("""
            SELECT book
            FROM Book book
            WHERE book.archived = false
            AND book.shareable = true
            """)
    Page<Book> findAllDisplayableBooksForGuests(Pageable pageable);
}
