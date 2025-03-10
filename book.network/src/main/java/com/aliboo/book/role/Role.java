package com.aliboo.book.role;

import com.aliboo.book.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)

public class Role {

    @Id
    @GeneratedValue
    private Integer id;
    @Column(unique = true)
    private String name;

    @ManyToMany(mappedBy = "roles")
    @JsonIgnore //hna drnaha bsh matb9ash f fetcha user o role dima o dkhl f loop
    private List<User> users; //hna bch dir relationship bin user o role


    @CreatedDate //hna bsh ydir date li t creea fiha authomatic 3an tari9 hd jj var li ltht
    @Column(nullable = false , updatable = false) //hna bsh drr tkon endha 9ima o mtknsh kat updata bsh mdkhlsh me var lastModifiedDate
    private LocalDateTime createdDate;
    @LastModifiedDate
    @Column(insertable = false)//hna bsh tkon l3ks d var li fo9
    private LocalDateTime lastModifiedDate;
}
