package com.aliboo.book.book;

import com.aliboo.book.common.BaseEntity;
import com.aliboo.book.feedback.Feedback;
import com.aliboo.book.history.BookTransactionHistory;
import com.aliboo.book.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;


@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
//@EntityListeners(AuditingEntityListener.class)

public class Book extends BaseEntity { //l book kiyrit mn baseEntity (ayrit mno l id creat date o update ... )

    private String title;
    private String authorName;
    private String isbn; //hadi andiro fiha lbook number (identificaier)
    private String synopsis ;//resumer dl book
    private String bookCover; //pic dl book (ma andirohash f database bch matakhdsh espace andiroha f location f server)
    private boolean archived;
    private boolean shareable;

    //hna andiro relatioship bin l book o l user (one book endo one owner lihowa user wahed)
    @ManyToOne //hit bzff d books l one user drr dirha
    @JoinColumn(name = "owner_id")
    private User owner;

    //hna andiro relatioship bin l book o l feedback hna l3ks li lfo9(one book endo bzff d feedback)
    @OneToMany(mappedBy = "book")//book var drnaha fl feedback bch ndiro relationship bin feedback o book o drna fiha column f database bookid
    private List<Feedback> feedbacks;

    @OneToMany(mappedBy = "book")//book var drnaha fl BookTransactionHistory bch ndiro relationship bin BookTransactionHistory o book o drna fiha column f database bookid tahia
    private List<BookTransactionHistory> histories;



}
