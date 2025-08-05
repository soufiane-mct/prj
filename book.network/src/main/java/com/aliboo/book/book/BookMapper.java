package com.aliboo.book.book;

import com.aliboo.book.file.FileUtils;
import com.aliboo.book.history.BookTransactionHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookMapper {
    @Autowired
    private CategoryRepository categoryRepository;

    public Book toBook(BookRequest request) { //hna fin andiro save l book 3an tari9 request li endna li fiha vars li ay3mr user li deja jbnah 3an tari3 l auth
        Book.BookBuilder<?, ?> builder = Book.builder();
        builder.id(request.id())
                .title(request.title())
                .authorName(request.authorName())
                .location(request.location())
                .fullAddress(request.fullAddress())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .synopsis(request.synopsis())
                .archived(false)//hna fsh ydir save mykonch bookfl archive talabgha yrdha true
                .shareable(request.shareable());
        if (request.categoryId() != null) {
            builder.category(categoryRepository.findById(request.categoryId()).orElse(null));
        }
        return builder.build();
    }

    public BookResponse toBookResponse(Book book) { //hna fin andiro getbook by id mn bookresponse lifiha data o an3iyto liha fl bookservice bch flkhr andiroha fl bookcontroller
        return BookResponse.builder()
                .id(book.getId())
                .location(book.getLocation())
                .fullAddress(book.getFullAddress())
                .latitude(book.getLatitude())
                .longitude(book.getLongitude())
                .title(book.getTitle())
                .authorName(book.getAuthorName())
                .synopsis(book.getSynopsis())
                .rate(book.getRate())
                .archived(book.isArchived())
                .shareable(book.isShareable())
                .owner(book.getOwner().fullName())
                .cover(FileUtils.readFileFromLocation(book.getBookCover()))
                .categoryName(book.getCategory() != null ? book.getCategory().getName() : null)
                .build();
    }

    public BorrowedBookResponse toBorrowedBookResponse(BookTransactionHistory history) {
        return BorrowedBookResponse.builder()
                .id(history.getBook().getId()) //mn BookTransactionHistory dkhl l book o jib l id dyalo o dir lih build f BorrowedBookResponse 3an tari9 l function toBorrowedBookResponse o li ankhdmo biha f Bookservice
                .title(history.getBook().getTitle())
                .authorName(history.getBook().getAuthorName())
                .location(history.getBook().getLocation())
                .rate(history.getBook().getRate())
                .returned(history.isReturned()) //hdo ra a mn BookTransactionHistory
                .returnApproved(history.isReturnApproved())
                .build();
    }
}
