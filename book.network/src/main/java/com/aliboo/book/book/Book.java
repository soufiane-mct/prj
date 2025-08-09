
package com.aliboo.book.book;

import com.aliboo.book.common.BaseEntity;
import com.aliboo.book.feedback.Feedback;
import com.aliboo.book.history.BookTransactionHistory;
import com.aliboo.book.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
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
    private String location; // Morocco city for the book (short name)
    private String fullAddress; // Full human-readable address (e.g., Rocade Express de Rabat, El Youssoufia, ...)
    private Double latitude; // latitude for geolocation
    private Double longitude; // longitude for geolocation
    private String synopsis ;//resumer dl book
//    private String bookCover; //pic dl book (ma andirohash f database bch matakhdsh espace andiroha f location f server)
    private boolean archived;
    private boolean shareable;

    //hna andiro relatioship bin l book o l user (one book endo one owner lihowa user wahed)
    @ManyToOne //hit bzff d books l one user drr dirha
    @JoinColumn(name = "owner_id")
    private User owner;

    //hna andiro relatioship bin l book o l feedback hna l3ks li lfo9(one book endo bzff d feedback)
    @OneToMany(mappedBy = "book", cascade = CascadeType.REMOVE, orphanRemoval = true) // Automatically remove feedbacks when book is deleted
    @Builder.Default
    private List<Feedback> feedbacks = new ArrayList<>();


    @OneToMany(mappedBy = "book", cascade = CascadeType.REMOVE, orphanRemoval = true) // Automatically remove histories when book is deleted
    @Builder.Default
    private List<BookTransactionHistory> histories = new ArrayList<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.REMOVE, orphanRemoval = true) // Automatically remove guest rent requests when book is deleted
    @Builder.Default
    private List<GuestRentRequest> guestRentRequests = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("isCover DESC, id ASC") // Cover image first, then others by ID
    @Builder.Default
    private List<BookImage> images = new ArrayList<>();
    
    @Column(name = "video_url", length = 1000)
    private String videoUrl;
    
    // Keep the old bookCover field for backward compatibility
    @Transient
    public String getBookCover() {
        return images.stream()
                .filter(BookImage::isCover)
                .findFirst()
                .map(BookImage::getImageUrl)
                .orElse(null);
    }
    
    @Transient
    public void setBookCover(String imageUrl) {
        // This is a no-op setter for backward compatibility
        // The actual setting should be done through the images collection
    }

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
