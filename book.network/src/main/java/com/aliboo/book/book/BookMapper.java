package com.aliboo.book.book;

import com.aliboo.book.history.BookTransactionHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Service
public class BookMapper {
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Value("${application.base-url:http://localhost:8081}")
    private String baseUrl;

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
        // Get all cover images (where isCover is true) and transform to URLs
        List<String> coverImages = book.getImages().stream()
                .filter(BookImage::isCover)
                .map(image -> {
                    // Convert local path to URL
                    String path = image.getImageUrl();
                    // Handle both forward and backward slashes
                    String filename = path.substring(Math.max(
                        path.lastIndexOf('/'), 
                        path.lastIndexOf('\\')
                    ) + 1);
                    return baseUrl + "/api/v1/books/" + book.getId() + "/images/" + filename;
                })
                .toList();
                
        // If no cover images, use the first image if available
        if (coverImages.isEmpty() && !book.getImages().isEmpty()) {
            String firstImageUrl = book.getImages().get(0).getImageUrl();
            String filename = firstImageUrl.substring(Math.max(
                firstImageUrl.lastIndexOf('/'), 
                firstImageUrl.lastIndexOf('\\')
            ) + 1);
            coverImages = List.of(baseUrl + "/api/v1/books/" + book.getId() + "/images/" + filename);
        }
        
        // Build the video URL if it exists
        String videoUrl = null;
        if (book.getVideoUrl() != null && !book.getVideoUrl().isBlank()) {
            String videoFilename = book.getVideoUrl().substring(Math.max(
                book.getVideoUrl().lastIndexOf('/'), 
                book.getVideoUrl().lastIndexOf('\\')
            ) + 1);
            videoUrl = baseUrl + "/api/v1/books/" + book.getId() + "/videos/" + videoFilename;
        }
        
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
                .cover(coverImages) // Set the list of cover image URLs
                .videoUrl(videoUrl) // Set the video URL
                .categoryName(book.getCategory() != null ? book.getCategory().getName() : null)
                .categoryId(book.getCategory() != null ? book.getCategory().getId() : null)
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
