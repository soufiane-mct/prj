package com.aliboo.book.user;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity


public class Token {
    @Id
    @GeneratedValue
    private Integer id;
    private String token ;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime validateAt;


    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE) // Cascade delete tokens when user is deleted
    private User user;

}















































