package com.aliboo.book.history;

import com.aliboo.book.book.Book;
import com.aliboo.book.common.BaseEntity;
import com.aliboo.book.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

public class BookTransactionHistory extends BaseEntity { //hna atrit l vars d baseentity

    //hna andiro relationship bl user ohna l user y9d ykono endo many transaction history ms kola user endo many transaction
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    //hna andiro relationship bl book kola book endo bzff transaction history
    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;


    private boolean returned; //var atnshofo biha wsh l boo rje3 ola no
    private boolean returnApproved;//hna mol l ktab fsh ayrej3o lih lktab khas y3ti l approvment



}
