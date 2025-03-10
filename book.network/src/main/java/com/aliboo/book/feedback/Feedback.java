package com.aliboo.book.feedback;

import com.aliboo.book.book.Book;
import com.aliboo.book.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Feedback extends BaseEntity { //hna anrito dakshi li f BaseEntity jibo lia

    private Double note; //t9yim mn 1-5 stars
    private String comment;

    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book; //hna bch creina relationship bin book o feedback (one book fih bzff feedback dkhl l feedback bch tfhm)

}
