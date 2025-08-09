package com.aliboo.book.book;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookResponse {
    private Integer id;
    private String title;
    private String authorName;
    private String location;
    private String fullAddress;
    private Double latitude;
    private Double longitude;
    private String synopsis;
    private String owner;
    private List<String> cover; // List of image URLs
    private double rate;
    private boolean archived;
    private boolean shareable;
    private String categoryName;
    private Integer categoryId; // Added to match the BookMapper update
    private String videoUrl; // URL for the book's video
}
