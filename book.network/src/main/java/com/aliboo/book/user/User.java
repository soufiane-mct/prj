package com.aliboo.book.user;

import com.aliboo.book.book.Book;
import com.aliboo.book.history.BookTransactionHistory;
import com.aliboo.book.role.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "app_user")
@EntityListeners(AuditingEntityListener.class)
public class User implements UserDetails, Principal { //Principal ankhdmo biha mn b3d fl authenticationService mnha anjibo l user a mn authenticate 3an tari9 Principal bla mnjiboh mn database

    @Id
    @GeneratedValue
    private Integer id;
    private String firstname;
    private String lastname;
    private String dateOfBirth;
    @Column(unique = true)
    private String email;
    private String password;
    private boolean accountLocked;
    private boolean enabled;

    //ManyToMany zeama kynin bzff dl users o kola user possible ykon endo bzff d roles (ta role atl9a fiha ManyToMany hna fin creeina hd relationship binathom)
    @ManyToMany(fetch = FetchType.EAGER) //hna fs y fetchi l user y fetchi meah ta list role
    private List<Role> roles;//drna relationship bin user o role

    //hna andiro relatioship bin l book o l user (one User ykono endo bzff dl books)
    @OneToMany(mappedBy = "owner") //hit one user to many books drr dirha o l mapping dyalha hia l var owner li dernaha fl book bch rebto relationship bl User ol book
    @JsonIgnore
    private List<Book> books;

    //hna relationship bl user o BookTransactionHistory one user endo bzff transaction historie
    @OneToMany(mappedBy = "user")//user var drnaha fl BookTransactionHistory bch ndiro relationship bin BookTransactionHistory o user o drna fiha column f database userid
    @JsonIgnore
    private List<BookTransactionHistory> histories;


    @CreatedDate //hna bsh ydir date li t creea fiha authomatic 3an tari9 hd jj var li ltht
    @Column(nullable = false , updatable = false) //hna bsh drr tkon endha 9ima o mtknsh kat updata bsh mdkhlsh me var lastModifiedDate
    private LocalDateTime createdDate;
    @LastModifiedDate
    @Column(insertable = false)//hna bsh tkon l3ks d var li fo9
    private LocalDateTime lastModifiedDate;

    //hadshi li tht function d userdetail kat3tihom lina o kn3mrohom bl var dyalna li ankhdmo bihom mn b3d
    @Override
    public String getName() {
        return email; //hna khetarina email li nakhdo mno name dl user (hit hwa li unique)
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles
                .stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName())) // Ensure Spring Security recognizes roles
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
//hna lbghiti doz w9ita eadyt expira l user
        //    return accountExpirationDate != null && accountExpirationDate.isAfter(LocalDate.now());
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public String fullName(){ //hna labghina njibo l full name
        return firstname + " " + lastname;
    } //hdi khdmna biha fl mail fl authservice
}
