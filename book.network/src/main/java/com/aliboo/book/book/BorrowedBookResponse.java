package com.aliboo.book.book;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class BorrowedBookResponse {
    private Integer id;
    private String title;
    private String authorName;
    private String location;
    private String synopsis;
    private double rate;
    private boolean returned;//nshofo wsh rj3 lbook wl no
    private boolean returnApproved;

}
