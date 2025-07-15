package com.aliboo.book.book;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookResponse {
    private Integer id;
    private String title;
    private String authorName;
    private String isbn;
    private String synopsis;
    private String owner;
    private byte[] cover;//arr mno3 byte
    private double rate;//t9yim dl avrege d all lfeedback(lkno 5 feedback o kmlin etaw 5 stars adir 5*5/5) hesbnaha fl book class
    private boolean archived;
    private boolean shareable;
    private String categoryName;
}
