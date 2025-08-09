package com.aliboo.book.book;

import com.aliboo.book.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "book_images")
public class BookImage extends BaseEntity {
    
    @Column(nullable = false)
    private String imageUrl;
    
    @Column(nullable = false)
    @Builder.Default
    private boolean isCover = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;
    
    // Helper method to create a new BookImage
    public static BookImage of(String imageUrl, boolean isCover, Book book) {
        return BookImage.builder()
                .imageUrl(imageUrl)
                .isCover(isCover)
                .book(book)
                .build();
    }
}
