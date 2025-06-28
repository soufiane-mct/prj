package com.aliboo.book.common;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> { // dik T bch PageResponse takhd BookResponse hit andiro PageResponse<BookResponse> fl book sevice
    private List<T> content;
    private int number;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    //bch nearfo wsh page the last one ola first one
}
