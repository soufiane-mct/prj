package com.aliboo.book.book;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookImageRepository extends JpaRepository<BookImage, Long> {
    List<BookImage> findByBookIdOrderByIsCoverDescIdAsc(Integer bookId);
    
    /**
     * Find all book images for a specific book
     * @param bookId The ID of the book
     * @return List of book images for the specified book
     */
    List<BookImage> findByBookId(Integer bookId);
    
    void deleteByBookId(Integer bookId);
    void deleteByIdAndBookId(Long id, Integer bookId);
    
    @Modifying
    @Query("UPDATE BookImage bi SET bi.isCover = false WHERE bi.book.id = :bookId AND bi.isCover = true")
    int unsetExistingCovers(@Param("bookId") Integer bookId);
    
    /**
     * Find all book images in the system (for debugging purposes)
     * @return Non-null list of all book images
     */
    @Override
    @org.springframework.lang.NonNull
    List<BookImage> findAll();
}
