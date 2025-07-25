package com.aliboo.book.book;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/guest-rent")
@RequiredArgsConstructor
public class GuestRentRequestController {
    private final GuestRentRequestRepository guestRentRequestRepository;
    private final BookRepository bookRepository;

    @PostMapping
public ResponseEntity<?> createGuestRent(@RequestBody GuestRentRequestDto dto) {
    Book book = bookRepository.findById(dto.bookId()).orElse(null);
    if (book == null) return ResponseEntity.badRequest().body("Book not found");
    GuestRentRequest req = GuestRentRequest.builder()
            .book(book)
            .name(dto.name())
            .lastname(dto.lastname())
            .phone(dto.phone())
            .location(dto.location())
            .build();
    guestRentRequestRepository.save(req);
    return ResponseEntity.noContent().build();
}

    @GetMapping("/owner")
    public List<GuestRentRequest> getOwnerGuestRents(Authentication auth) {
        // Only show requests for books owned by the authenticated user
        Integer ownerId = ((com.aliboo.book.user.User) auth.getPrincipal()).getId();
        return guestRentRequestRepository.findByBook_Owner_Id(ownerId);
    }

    public record GuestRentRequestDto(Integer bookId, String name, String lastname, String phone, String location) {}
}