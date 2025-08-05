package com.aliboo.book.book;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.aliboo.book.common.PageResponse;
import org.springframework.web.multipart.MultipartFile;
import com.aliboo.book.user.User;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@Tag(name = "Book")
public class BookController {
    private final BookService service;
    private final BookRepository bookRepository;

    @GetMapping("/filter-by-location")
    public ResponseEntity<PageResponse<BookResponse>> filterByLocation(
        @RequestParam(name = "location", required = false) String location,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.filterByLocation(location, page, size));
    }
    
    @GetMapping("/debug/geolocation")
    public ResponseEntity<?> debugGeolocationQuery(
        @RequestParam(name = "lat") double lat,
        @RequestParam(name = "lng") double lng,
        Authentication authentication
    ) {
        try {
            Integer userId = null;
            if (authentication != null && authentication.getPrincipal() != null) {
                userId = ((User) authentication.getPrincipal()).getId();
            }
            List<Object[]> results = bookRepository.debugGeolocationQuery(lat, lng, userId);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "count", results.size(),
                "data", results.stream().map(Arrays::toString).collect(Collectors.toList())
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", e.getMessage(),
                "cause", e.getCause() != null ? e.getCause().getMessage() : "No cause"
            ));
        }
    }


    //hna andiro save book
    @PostMapping
    public ResponseEntity<Integer> saveBook( //int l id dl book
            @Valid @RequestBody BookRequest request,
             //hna ltht bch njibo l connected user li andirolih save l book dyalo
             Authentication connectedUser
    ){
        return ResponseEntity.ok(service.save(request, connectedUser));//dir save l book 3an tari9 tshofrequest o l connectedUser
    }
    //hna bch ndiro get l book by id
    @GetMapping("{book-id}")
    public ResponseEntity<BookResponse> findBookById(//BookResponse fiha gae data dl book o anjiboha 3an tari9 bookid
            @PathVariable("book-id") Integer bookId //jib lia l id mn hd l path o dirha f var bookId
    ){
        return ResponseEntity.ok(service.findById(bookId));//findById
    }

    //hna find all books (manjibohomsh kmlin f d9a whda ms 3an tari9 pageing)
    @GetMapping
    public ResponseEntity<PageResponse<BookResponse>> findAllBooks(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size,
            @RequestParam(name = "location", required = false) String location,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "categoryId", required = false) Integer categoryId,
            @RequestParam(name = "lat", required = false) Double lat,
            @RequestParam(name = "lng", required = false) Double lng,
            @RequestParam(name = "radius", required = false) Double radius,
            Authentication connectedUser
    ) {
        // Validate that if lat or lng is provided, the other must also be provided
        if ((lat != null && lng == null) || (lat == null && lng != null)) {
            throw new IllegalArgumentException("Both latitude and longitude must be provided together");
        }
        
        // If coordinates are provided, radius is required
        if (lat != null && lng != null && (radius == null || radius <= 0)) {
            throw new IllegalArgumentException("A positive radius in kilometers is required when using coordinates");
        }
        
        // Ensure page and size are within reasonable bounds
        int validatedPage = Math.max(0, page);
        int validatedSize = Math.min(50, Math.max(1, size)); // Limit page size to 50
        
        return ResponseEntity.ok(service.findAllBooks(
            validatedPage, 
            validatedSize, 
            location, 
            search, 
            categoryId, 
            lat, 
            lng, 
            radius, 
            connectedUser
        ));
    }
    //hna an fetshiw all books by owner
    @GetMapping("/owner")
    public ResponseEntity<PageResponse<BookResponse>> findAllBooksByOwner(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page, //kna default page hia 0 lihia page lwla ead user ybda ybdl pages
            @RequestParam(name = "size", defaultValue = "10", required = false) int size, //hna size dl pages ktshd 0 - 10 (5-15..)
            @RequestParam(name = "location", required = false) String location,
            @RequestParam(name = "lat", required = false) Double lat,
            @RequestParam(name = "lng", required = false) Double lng,
            @RequestParam(name = "radius", required = false) Double radius,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(service.findAllBooksByOwner(page, size, location, lat, lng, radius, connectedUser));
    }


    //hna bch user yshof l borrowed book li kheda (l user fsh aybda ya khd l books o ydir lihom borrow)
    @GetMapping("/borrowed")
    public ResponseEntity<PageResponse<BorrowedBookResponse>> findAllBorrowedBooks( //BorrowedBookResponse khdmna biha f bookservice
            @RequestParam(name = "page", defaultValue = "0", required = false) int page ,//kna default page hia 0 lihia page lwla ead user ybda ybdl pages
            @RequestParam(name = "size", defaultValue = "10", required = false) int size ,//hna size dl pages ktshd 0 - 10 (5-15..)
            Authentication connectedUser //bch nshofo l borrowed books dl user
    ) {
        return ResponseEntity.ok(service.findAllBorrowedBooks(page, size, connectedUser));
    }
    //fsh l user borrow shi book khas yredo
    //khas nshofo returned book
    //l owner dl book khas yshof all returned books

    @GetMapping("/returned")
    public ResponseEntity<PageResponse<BorrowedBookResponse>> findAllReturnedBooks( //BorrowedBookResponse khdmna biha f bookservice
            @RequestParam(name = "page", defaultValue = "0", required = false) int page ,//kna default page hia 0 lihia page lwla ead user ybda ybdl pages
            @RequestParam(name = "size", defaultValue = "10", required = false) int size ,//hna size dl pages ktshd 0 - 10 (5-15..)
            Authentication connectedUser //bch nshofo l borrowed books dl user
    ) {
        return ResponseEntity.ok(service.findAllReturnedBooks(page, size, connectedUser));
    }

    //GetMapping katjib biha shi haja PostMapping kat antegri shi haja PatchMapping kat3del ela shi haja

    //update shareable-status bch ymkn l owner yrd l book dyalo shareable ola la
    @PatchMapping("/shareable/{book-id}")//katgolih i bghit n update sharbel dl book id mera whd b true ola false
    public ResponseEntity<Integer> updateShareableStatus(
            @PathVariable("book-id") Integer bookId, //dkh l bath o jib lia l9ima dl book-id o rbtha lia f var bookId
            Authentication connectedUser //o ykon l connectedUser
    ) {
        return ResponseEntity.ok(service.updateShareableStatus(bookId,connectedUser));
    }

    //update archived bch ymkn l owner yrd l book dyalo archived ola la
    @PatchMapping("/archived/{book-id}")//katgolih i bghit n update sharbel dl book id mera whd b true ola false
    public ResponseEntity<Integer> updateArchivedStatus(
            @PathVariable("book-id") Integer bookId, //dkh l bath o jib lia l9ima dl book-id o rbtha lia f var bookId
            Authentication connectedUser //o ykon l connectedUser
    ) {
        return ResponseEntity.ok(service.updateArchivedStatus(bookId,connectedUser));
    }

    //hna bach ndiro borrow l book hdi method mohima f hd project
    @PostMapping("/borrow/{book-id}")
    public ResponseEntity<Integer> borrowBook (
            @PathVariable("book-id") Integer bookId, //dkh l path o jib lia l9ima dl book-id o rbtha lia f var bookId
            Authentication connectedUser //o ykon l connectedUser
    ) {
        return ResponseEntity.ok(service.borrowBook(bookId, connectedUser));
    }

    //hna andiro returned borrowed book (bch l user yrj3 lktoba li krahom)
    @PatchMapping("/borrow/return/{book-id}")
    public ResponseEntity<Integer> returnBorrowBook (
            @PathVariable("book-id") Integer bookId,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(service.returnBorrowedBook(bookId, connectedUser));
    }

    //hna andiro approve l return borrowed book
    @PatchMapping("/borrow/return/approve/{book-id}")
    public ResponseEntity<Integer> approveReturnBorrowBook (
            @PathVariable("book-id") Integer bookId,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(service.approveReturnBorrowBook(bookId, connectedUser));
    }


    //hna andiro save l pic ms andiroha f service li andiro mshi f database
    @PostMapping(value = "/cover/{book-id}", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadBookCoverPicture(
            @PathVariable("book-id") Integer bookId,
            @RequestParam("file") MultipartFile file,
            Authentication connectedUser
    ){
        System.out.println("DEBUG: Received upload request for book ID: " + bookId);
        System.out.println("DEBUG: File name: " + file.getOriginalFilename());
        System.out.println("DEBUG: File size: " + file.getSize());
        service.uploadBookCoverPicture(file, connectedUser, bookId);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/cover/{book-id}")
    public ResponseEntity<byte[]> getBookCover(@PathVariable("book-id") Integer bookId) {
        Book book = service.getBookEntityById(bookId);
        if (book == null || book.getBookCover() == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            byte[] image = org.springframework.util.FileCopyUtils.copyToByteArray(new java.io.File(book.getBookCover()));
            return ResponseEntity
                .ok()
                .header("Content-Type", "image/png") // Change if you support other types
                .body(image);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{book-id}")
    public ResponseEntity<Void> deleteBook(
            @PathVariable("book-id") Integer bookId,
            Authentication connectedUser
    ) {
        service.deleteBook(bookId, connectedUser);
        return ResponseEntity.noContent().build();
    }

}

