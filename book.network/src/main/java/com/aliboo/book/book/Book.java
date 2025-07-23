
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
    @OneToMany(mappedBy = "book", cascade = CascadeType.REMOVE, orphanRemoval = true) // Automatically remove feedbacks when book is deleted
    private List<Feedback> feedbacks;


    @OneToMany(mappedBy = "book", cascade = CascadeType.REMOVE, orphanRemoval = true) // Automatically remove histories when book is deleted
    private List<BookTransactionHistory> histories;

    @OneToMany(mappedBy = "book", cascade = CascadeType.REMOVE, orphanRemoval = true) // Automatically remove guest rent requests when book is deleted
    private List<GuestRentRequest> guestRentRequests;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Transient double getRate() {
        if(feedbacks == null || feedbacks.isEmpty()){
            return 0.0;//hit double lamakan ta feed back o kn emty dir lia 0
        }
        var rate = this.feedbacks.stream()
                .mapToDouble(Feedback::getNote)//jib lia note mn feedback
                .average()//hna hseb lia l avrege dyal all note
                .orElse(0.0); //lmknsh tl3 0 hdshi kml diro f var rate
        //hna l tht andiro lkn ration matalan 2.33 dir lia 2 ola kan 2.60 dir lia 3
        double roundedRate = Math.round(rate*10.0) / 10.0;

        return roundedRate;
    }


}
