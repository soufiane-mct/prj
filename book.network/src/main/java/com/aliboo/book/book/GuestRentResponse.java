package com.aliboo.book.book;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record GuestRentResponse(
    Integer id,
    String name,
    String lastname,
    String phone,
    String location,
    LocalDateTime createdDate,
    String bookTitle,
    String bookAuthor
) {}
