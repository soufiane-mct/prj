package com.aliboo.book.book;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

//import java.awt.print.Pageable;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Transactional(readOnly = true)
public interface BookRepository extends JpaRepository<Book, Integer>, JpaSpecificationExecutor<Book> { //tania JpaSpecificationExecutor khdmna biha fl findAllBooksByOwner fl bookservice

    Logger logger = LoggerFactory.getLogger(BookRepository.class);

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
            AND (
              (:location IS NULL OR :location = '')
              OR (
                book.location IS NOT NULL
                AND book.location <> ''
                AND LOWER(book.location) LIKE LOWER(CONCAT('%', :location, '%'))
              )
            )
            """)
    Page<Book> findAllDisplayableBooksByLocation(Pageable pageable, Integer userId, String location);
    
    @Query("""
            SELECT book
            FROM Book book
            WHERE book.archived = false
            AND book.shareable = true
            AND book.owner.id != :userId
            AND (:search IS NULL OR :search = '' OR LOWER(book.title) LIKE LOWER(:search))
            AND (:categoryId IS NULL OR book.category.id = :categoryId)
            AND (
              (:location IS NULL OR :location = '')
              OR (
                book.location IS NOT NULL
                AND book.location <> ''
                AND LOWER(book.location) LIKE LOWER(CONCAT('%', :location, '%'))
              )
            )
            """)
    Page<Book> findAllDisplayableBooksWithFilters(
        Pageable pageable, 
        @Param("userId") Integer userId, 
        @Param("location") String location,
        @Param("search") String search,
        @Param("categoryId") Integer categoryId
    );
    
    @Query("""
            SELECT book
            FROM Book book
            WHERE book.archived = false
            AND book.shareable = true
            AND (:search IS NULL OR :search = '' OR LOWER(book.title) LIKE LOWER(:search))
            AND (:categoryId IS NULL OR book.category.id = :categoryId)
            AND (
              (:location IS NULL OR :location = '')
              OR (
                book.location IS NOT NULL
                AND book.location <> ''
                AND LOWER(book.location) LIKE LOWER(CONCAT('%', :location, '%'))
              )
            )
            """)
    Page<Book> findAllDisplayableBooksForGuestsWithFilters(
        Pageable pageable, 
        @Param("location") String location,
        @Param("search") String search,
        @Param("categoryId") Integer categoryId
    );
    
    @Query(value = """
        SELECT b.*, 
               (6371 * ACOS(
                   COS(RADIANS(:lat)) * COS(RADIANS(b.latitude)) * 
                   COS(RADIANS(b.longitude) - RADIANS(:lng)) + 
                   SIN(RADIANS(:lat)) * SIN(RADIANS(b.latitude))
               )) AS distance
        FROM book b
        WHERE b.archived = false
        AND b.shareable = true
        AND b.latitude IS NOT NULL
        AND b.longitude IS NOT NULL
        AND (:search IS NULL OR :search = '' OR LOWER(b.title) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:categoryId IS NULL OR b.category_id = :categoryId)
        AND (6371 * ACOS(
            COS(RADIANS(:lat)) * COS(RADIANS(b.latitude)) * 
            COS(RADIANS(b.longitude) - RADIANS(:lng)) + 
            SIN(RADIANS(:lat)) * SIN(RADIANS(b.latitude))
        )) < :radius
        ORDER BY distance, b.createddate DESC
        """, 
    countQuery = """
        SELECT COUNT(*)
        FROM book b
        WHERE b.archived = false
        AND b.shareable = true
        AND b.latitude IS NOT NULL
        AND b.longitude IS NOT NULL
        AND (:search IS NULL OR :search = '' OR LOWER(b.title) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:categoryId IS NULL OR b.category_id = :categoryId)
        AND (6371 * ACOS(
            COS(RADIANS(:lat)) * COS(RADIANS(b.latitude)) * 
            COS(RADIANS(b.longitude) - RADIANS(:lng)) + 
            SIN(RADIANS(:lat)) * SIN(RADIANS(b.latitude))
        )) < :radius
        """,
    nativeQuery = true)
    default Page<Book> findAllBooksNearby(
        Pageable pageable,
        @Param("lat") double lat,
        @Param("lng") double lng,
        @Param("radius") double radius,
        @Param("search") String search,
        @Param("categoryId") Integer categoryId
    ) {
        logger.info("Executing findAllBooksNearby with params: lat={}, lng={}, radius={}, search='{}', categoryId={}", 
            lat, lng, radius, search, categoryId);
        try {
            Page<Book> result = _findAllBooksNearby(pageable, lat, lng, radius, search, categoryId);
            logger.info("findAllBooksNearby query executed successfully. Found {} books", result.getTotalElements());
            return result;
        } catch (Exception e) {
            logger.error("Error in findAllBooksNearby: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Query(value = """
        SELECT b.*, 
               (6371 * ACOS(
                   COS(RADIANS(:lat)) * COS(RADIANS(b.latitude)) * 
                   COS(RADIANS(b.longitude) - RADIANS(:lng)) + 
                   SIN(RADIANS(:lat)) * SIN(RADIANS(b.latitude))
               )) AS distance
        FROM book b
        WHERE b.archived = false
        AND b.shareable = true
        AND b.latitude IS NOT NULL
        AND b.longitude IS NOT NULL
        AND (:search IS NULL OR :search = '' OR LOWER(b.title) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:categoryId IS NULL OR b.category_id = :categoryId)
        AND (6371 * ACOS(
            COS(RADIANS(:lat)) * COS(RADIANS(b.latitude)) * 
            COS(RADIANS(b.longitude) - RADIANS(:lng)) + 
            SIN(RADIANS(:lat)) * SIN(RADIANS(b.latitude))
        )) < :radius
        ORDER BY distance, b.createddate DESC
        """, 
    countQuery = """
        SELECT COUNT(*)
        FROM book b
        WHERE b.archived = false
        AND b.shareable = true
        AND b.latitude IS NOT NULL
        AND b.longitude IS NOT NULL
        AND (:search IS NULL OR :search = '' OR LOWER(b.title) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:categoryId IS NULL OR b.category_id = :categoryId)
        AND (6371 * ACOS(
            COS(RADIANS(:lat)) * COS(RADIANS(b.latitude)) * 
            COS(RADIANS(b.longitude) - RADIANS(:lng)) + 
            SIN(RADIANS(:lat)) * SIN(RADIANS(b.latitude))
        )) < :radius
        """,
    nativeQuery = true)
    Page<Book> _findAllBooksNearby(
        Pageable pageable,
        @Param("lat") double lat,
        @Param("lng") double lng,
        @Param("radius") double radius,
        @Param("search") String search,
        @Param("categoryId") Integer categoryId
    );
    
    // Add a debug method to test the query directly
    @Query(value = """
        SELECT b.id, b.title, b.location, b.latitude, b.longitude,
               (6371 * acos(cos(radians(:lat)) * cos(radians(b.latitude)) * 
                cos(radians(b.longitude) - radians(:lng)) + 
                sin(radians(:lat)) * sin(radians(b.latitude)))) as distance
        FROM Book b
        WHERE b.archived = false
        AND b.shareable = true
        AND b.latitude IS NOT NULL
        AND b.longitude IS NOT NULL
        AND (b.owner_id != :userId OR :userId IS NULL)  -- Exclude books owned by the user if userId is provided
        HAVING distance < 50  -- 50km radius
        ORDER BY distance
        """, nativeQuery = true)
    List<Object[]> debugGeolocationQuery(
        @Param("lat") double lat,
        @Param("lng") double lng,
        @Param("userId") Integer userId
    );

    @Query("""
            SELECT book
            FROM Book book
            WHERE book.archived = false
            AND book.shareable = true
            AND (
              (:location IS NULL OR :location = '')
              OR (
                book.location IS NOT NULL
                AND book.location <> ''
                AND LOWER(book.location) LIKE LOWER(CONCAT('%', :location, '%'))
              )
            )
            """)
    Page<Book> findAllDisplayableBooksForGuestsByLocation(Pageable pageable, String location);

    @Query("""
        SELECT book FROM Book book
        WHERE book.archived = false
        AND book.shareable = true
        AND book.latitude IS NOT NULL AND book.longitude IS NOT NULL
        AND (
            6371 * acos(
                cos(radians(:lat)) * cos(radians(book.latitude)) *
                cos(radians(book.longitude) - radians(:lng)) +
                sin(radians(:lat)) * sin(radians(book.latitude))
            )
        ) < :radius
        """)
    Page<Book> findAllBooksNearby(Pageable pageable, @org.springframework.data.repository.query.Param("lat") double lat, @org.springframework.data.repository.query.Param("lng") double lng, @org.springframework.data.repository.query.Param("radius") double radius);

    @Query("""
            SELECT book
            FROM Book book
            WHERE book.archived = false
            AND book.shareable = true
            AND book.owner.id != :userId
            """)
    Page<Book> findAllDisplayableBooks(Pageable pageable, Integer userId);

    @Query("""
        SELECT book
        FROM Book book
        WHERE book.owner.id = :ownerId
        AND (:location IS NULL OR LOWER(book.location) LIKE LOWER(CONCAT('%', :location, '%')))
        ORDER BY book.createdDate DESC
        """)
    Page<Book> findByOwnerIdAndLocationContainingIgnoreCase(
        @org.springframework.data.repository.query.Param("ownerId") Integer ownerId,
        @org.springframework.data.repository.query.Param("location") String location,
        Pageable pageable
    );

    /**
     * Find all books owned by a specific user that fall within the specified bounding box.
     * This is used as a fast filter before applying the more accurate distance calculation.
     * 
     * @param ownerId The ID of the book owner
     * @param minLat Minimum latitude of the bounding box
     * @param maxLat Maximum latitude of the bounding box
     * @param minLng Minimum longitude of the bounding box
     * @param maxLng Maximum longitude of the bounding box
     * @return List of books within the bounding box
     */
    @Query("""
        SELECT b FROM Book b 
        WHERE b.owner.id = :ownerId 
        AND b.latitude IS NOT NULL 
        AND b.longitude IS NOT NULL
        AND b.latitude BETWEEN :minLat AND :maxLat
        AND b.longitude BETWEEN :minLng AND :maxLng
    """)
    List<Book> findByOwnerIdAndCoordinatesWithin(
        @Param("ownerId") Integer ownerId,
        @Param("minLat") double minLat,
        @Param("maxLat") double maxLat,
        @Param("minLng") double minLng,
        @Param("maxLng") double maxLng
    );

    @Query("""
        SELECT book
        FROM Book book
        WHERE book.owner.id = :ownerId
        ORDER BY book.createdDate DESC
        """)
    Page<Book> findByOwnerId(
        @org.springframework.data.repository.query.Param("ownerId") Integer ownerId,
        Pageable pageable
    );

    @Query("""
            SELECT book
            FROM Book book
            WHERE book.archived = false
            AND book.shareable = true
            """)
    Page<Book> findAllDisplayableBooksForGuests(Pageable pageable);
    @Query("""
        SELECT book
        FROM Book book
        WHERE (:location IS NULL OR LOWER(book.location) LIKE LOWER(CONCAT('%', :location, '%')))
        AND book.archived = false
        AND book.shareable = true
        """)
    Page<Book> findByLocationContainingIgnoreCase(@org.springframework.data.repository.query.Param("location") String location, org.springframework.data.domain.Pageable pageable);
}
