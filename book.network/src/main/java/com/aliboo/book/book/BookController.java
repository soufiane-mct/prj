package com.aliboo.book.book;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.aliboo.book.common.PageResponse;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@Tag(name = "Book")
public class BookController {
    private final BookService service;

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
    public ResponseEntity<PageResponse<BookResponse>> findAllBooks( //PageResponse drnaha fl common drna fiha vars dl pages o wst l pages anjibo all books on9smohom ela l pages li endna
            @RequestParam(name = "page", defaultValue = "0", required = false) int page ,//kna default page hia 0 lihia page lwla ead user ybda ybdl pages
            @RequestParam(name = "size", defaultValue = "10", required = false) int size ,//hna size dl pages ktshd 0 - 10 (5-15..)
            //hna anjibo all books mn users kherin exepte l connected user o andiro method khra find all books by owner
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(service.findAllBooks(page, size, connectedUser));
    }
    //hna an fetshiw all books by owner
    @GetMapping("/owner")
    public ResponseEntity<PageResponse<BookResponse>> findAllBooksByOwner(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page ,//kna default page hia 0 lihia page lwla ead user ybda ybdl pages
            @RequestParam(name = "size", defaultValue = "10", required = false) int size ,//hna size dl pages ktshd 0 - 10 (5-15..)
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(service.findAllBooksByOwner(page, size, connectedUser));
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

}

