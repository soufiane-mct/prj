package com.aliboo.book.book;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class GuestRentRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "book_id")
    @JsonIgnore
    private Book book;

    private String name;
    private String lastname;
    private String phone;
    private String location;
    private LocalDateTime createdDate;

    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
    }
} 