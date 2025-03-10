package com.aliboo.book.user;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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


    @ManyToOne //hna bsh possible ykon bzff d token l user whd
    @JoinColumn(name = "userId", nullable = false)
    private User user;

}















































