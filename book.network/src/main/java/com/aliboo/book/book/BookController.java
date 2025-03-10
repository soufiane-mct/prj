package com.aliboo.book.book;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("books")
@RequiredArgsConstructor
@Tag(name = "Book")

public class BookController {
    private final BookService service;

    //hna andiro save book
    @PostMapping
    public ResponseEntity<Integer> saveBook( //int l id dl book
            @Valid @RequestBody BookRequest request,
             //hna bch njibo l connected user li andirolih save l book dyalo
             Authentication connectedUser
    ){
        return ResponseEntity.ok(service.save(request, connectedUser));//dir save l book 3an tari9 tshofrequest o l connectedUser
    }
}

