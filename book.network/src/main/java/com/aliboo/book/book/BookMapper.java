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
                .isbn(request.isbn())
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
                .title(book.getTitle())
                .authorName(book.getAuthorName())
                .isbn(book.getIsbn())
                .synopsis(book.getSynopsis())
                .rate(book.getRate())
                .archived(book.isArchived())
                .shareable(book.isShareable())
                .owner(book.getOwner().fullName())
                .cover(FileUtils.readFileFromLocation(book.getBookCover())) // read l file dl cover li aysifto l user 3an tari9 class li drna FileUtils
                .categoryName(book.getCategory() != null ? book.getCategory().getName() : null)
                .build();
    }

    public BorrowedBookResponse toBorrowedBookResponse(BookTransactionHistory history) {
        return BorrowedBookResponse.builder()
                .id(history.getBook().getId()) //mn BookTransactionHistory dkhl l book o jib l id dyalo o dir lih build f BorrowedBookResponse 3an tari9 l function toBorrowedBookResponse o li ankhdmo biha f Bookservice
                .title(history.getBook().getTitle())
                .authorName(history.getBook().getAuthorName())
                .isbn(history.getBook().getIsbn())
                .rate(history.getBook().getRate())
                .returned(history.isReturned()) //hdo ra a mn BookTransactionHistory
                .returnApproved(history.isReturnApproved())
                .build();
    }
}
