package com.aliboo.book.book;

import org.springframework.stereotype.Service;

@Service
public class BookMapper {
    public Book toBook(BookRequest request) { //hna fin andiro save l book 3an tari9 request li endna li fiha vars li ay3mr user li deja jbnah 3an tari3 l auth
        return Book.builder()
                .id(request.id())
                .title(request.title())
                .authorName(request.authorName())
                .synopsis(request.synopsis())
                .archived(false)//hna fsh ydir save mykonch bookfl archive talabgha yrdha true
                .shareable(request.shareable())
                .build();
    }
}
