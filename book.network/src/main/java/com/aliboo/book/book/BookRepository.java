package com.aliboo.book.book;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

//import java.awt.print.Pageable;
import org.springframework.data.domain.Pageable;

public interface BookRepository extends JpaRepository<Book, Integer>, JpaSpecificationExecutor<Book> { //tania JpaSpecificationExecutor khdmna biha fl findAllBooksByOwner fl bookservice

    @Query("""
            SELECT book
            FROM Book book
            WHERE book.archived = false
            AND book.shareable = true
            AND book.owner.id != :userId
            """) //hna mn book jib lia l book li mshi archived o li shareable o user mykonsh mol lktab bch mytl3osh lih ktoba dyalo (userId li howa li andiro ltht)
    Page<Book> findAllDisplayableBooks(Pageable pageable, Integer userId);
}
