package com.aliboo.book.book;

import com.aliboo.book.common.PageResponse;
import com.aliboo.book.file.FileStorageService;
import com.aliboo.book.exception.OperationNotPermitedException;
import com.aliboo.book.history.BookTransactionHistory;
import com.aliboo.book.history.BookTransactionHistoryRepository;
import com.aliboo.book.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;


@Service
@RequiredArgsConstructor
public class BookService {

    public PageResponse<BookResponse> filterByLocation(String location, int page, int size) {
        var pageable = org.springframework.data.domain.PageRequest.of(page, size);
        var books = bookRepository.findByLocationContainingIgnoreCase(location, pageable);
        var content = books.getContent().stream().map(bookMapper::toBookResponse).toList();
        return new com.aliboo.book.common.PageResponse<BookResponse>(
            content,
            books.getNumber(),
            books.getSize(),
            books.getTotalElements(),
            books.getTotalPages(),
            books.isFirst(),
            books.isLast()
        );
    }

    private final BookMapper bookMapper;
    private final BookRepository bookRepository;
    private final BookTransactionHistoryRepository transactionHistoryRepository;
    private final BookImageService bookImageService;
    private final FileStorageService fileStorageService;

    @Transactional
    public void uploadBookCoverPicture(MultipartFile file, Authentication connectedUser, Integer bookId) {
        // Get the authenticated user
        User user = (User) connectedUser.getPrincipal();
        
        // Wrap single file in a list and upload as the first (cover) image
        List<MultipartFile> files = new ArrayList<>();
        files.add(file);
        bookImageService.uploadBookImages(bookId, files, true, user.getId());
    }
    
    /**
     * Uploads a video file for a book
     * @param file The video file to upload
     * @param connectedUser The authenticated user
     * @param bookId The ID of the book to associate the video with
     * @throws EntityNotFoundException if the book is not found
     * @throws OperationNotPermitedException if the user is not the owner of the book
     * @throws RuntimeException if there's an error uploading the file
     */
    @Transactional
    public void uploadBookVideo(MultipartFile file, Authentication connectedUser, Integer bookId) {
        log.info("=== STARTING VIDEO UPLOAD FOR BOOK ID: {} ===", bookId);
        log.info("Original filename: {}", file.getOriginalFilename());
        log.info("Content type: {}", file.getContentType());
        log.info("File size: {} bytes", file.getSize());
        
        // Get the authenticated user
        User user = (User) connectedUser.getPrincipal();
        log.info("Authenticated user ID: {}", user.getId());
        
        // Find the book
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> {
                    String errorMsg = "No book found with ID: " + bookId;
                    log.error(errorMsg);
                    return new EntityNotFoundException(errorMsg);
                });
        
        // Verify the user is the owner of the book
        if (!book.getOwner().getId().equals(user.getId())) {
            String errorMsg = String.format("User %s is not authorized to upload videos for book %d", 
                    user.getEmail(), bookId);
            log.error(errorMsg);
            throw new OperationNotPermitedException("You are not allowed to upload videos for this book");
        }
        
        try {
            // Create a clean subdirectory path for the video
            String bookSubPath = String.format("books/user_%d/book_%d/videos", user.getId(), bookId);
            log.info("Uploading video to subdirectory: {}", bookSubPath);
            
            // Ensure the filename is not null
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.trim().isEmpty()) {
                originalFilename = "video_" + System.currentTimeMillis() + ".mp4";
                log.warn("Original filename was null or empty, using generated name: {}", originalFilename);
            }
            
            // Create a new MultipartFile with the ensured filename
            CustomMultipartFile customFile = new CustomMultipartFile(
                file.getBytes(),
                originalFilename,
                file.getContentType() != null ? file.getContentType() : "video/mp4"
            );
            
            // Upload the video file
            log.info("Starting file upload...");
            String videoUrl = fileStorageService.uploadFile(customFile, bookSubPath);
            
            if (videoUrl == null || videoUrl.trim().isEmpty()) {
                String errorMsg = "Failed to upload video: Empty URL returned from file storage service";
                log.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }
            
            log.info("Video uploaded successfully. Updating book with video URL: {}", videoUrl);
            
            // Update the book with the video URL
            book.setVideoUrl(videoUrl);
            book = bookRepository.save(book);
            
            log.info("Successfully updated book {} with video URL: {}", bookId, videoUrl);
            
        } catch (Exception e) {
            String errorMsg = String.format("Error uploading video for book ID %d: %s", bookId, e.getMessage());
            log.error(errorMsg, e);
            throw new RuntimeException("Failed to upload video: " + e.getMessage(), e);
        }
    }

    public Integer save(BookRequest request, Authentication connectedUser) {
        // Get the authenticated user
        User user = ((User) connectedUser.getPrincipal());
        
        // Validate location data
        if (request.location() == null || request.location().trim().isEmpty()) {
            throw new IllegalArgumentException("Location name is required");
        }
        if (request.fullAddress() == null || request.fullAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("Full address is required");
        }
        if (request.latitude() == null || request.longitude() == null) {
            throw new IllegalArgumentException("Latitude and longitude are required");
        }
        
        // Map the request to a Book entity
        Book book = bookMapper.toBook(request);
        
        // Set the owner and ensure location data is properly set
        book.setOwner(user);
        book.setLocation(request.location().trim());
        book.setFullAddress(request.fullAddress().trim());
        book.setLatitude(request.latitude());
        book.setLongitude(request.longitude());
        
        // Set createdBy to the current user's ID (mapped by auditing field name in BaseEntity)
        book.setCratedBy(user.getId());
        
        // Save the book
        return bookRepository.save(book).getId();
    }

    public BookResponse findById(Integer bookId) {

        return bookRepository.findById(bookId)//jib id whd mn bookRepository
                .map(bookMapper::toBookResponse)//mn dk id book lijbti fo9 jib gae data dyalo li drna fl BookResponse 3antari9 toBookResponse li drnaha f bookMapper
                .orElseThrow(()-> new EntityNotFoundException("No book found with the Id" + bookId));
    }


    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BookService.class);
    
    // Earth's radius in kilometers
    private static final double EARTH_RADIUS_KM = 6371.0;

    //fl PageResponse dir lia BookResponse (y3ni anjibo gae l books li kynin o an9smohom ela pages)
    public PageResponse<BookResponse> findAllBooks(int page, int size, String location, String search, Integer categoryId, Double lat, Double lng, Double radius, Authentication connectedUser) {
        log.info("Executing findAllBooks with params: page={}, size={}, location='{}', search='{}', categoryId={}, lat={}, lng={}, radius={}", 
            page, size, location, search, categoryId, lat, lng, radius);
            
        try {
            Integer userId = null;
            if (connectedUser != null && connectedUser.getPrincipal() != null) {
                userId = ((User) connectedUser.getPrincipal()).getId();
            }
            
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
            Page<Book> books;
            
            // Build search pattern for SQL LIKE query (only if search term is provided)
            String searchPattern = (search != null && !search.trim().isEmpty()) 
                ? "%" + search.trim().toLowerCase() + "%" 
                : null;
            
            // For location-based search (by coordinates and radius)
            if (lat != null && lng != null && radius != null && radius > 0) {
                log.info("Performing geospatial search with lat={}, lng={}, radius={}km, search='{}', categoryId={}", 
                    lat, lng, radius, search, categoryId);
                    
                // Use geospatial filtering with coordinates and radius
                books = bookRepository.findAllBooksNearby(
                    pageable, 
                    lat, 
                    lng, 
                    radius, 
                    searchPattern, 
                    categoryId
                );
                
                log.info("Geospatial search completed. Found {} books", books.getTotalElements());
            } 
            // For text-based location search (by city/neighborhood name)
            else if (location != null && !location.trim().isEmpty()) {
                log.info("Performing text-based location search for: '{}'", location);
                String locationPattern = "%" + location.trim().toLowerCase() + "%";
                
                if (userId != null) {
                    books = bookRepository.findAllDisplayableBooksByLocation(
                        pageable, 
                        userId, 
                        locationPattern
                    );
                } else {
                    books = bookRepository.findAllDisplayableBooksForGuestsByLocation(
                        pageable, 
                        locationPattern
                    );
                }
                log.info("Text-based location search completed. Found {} books for location: '{}'", 
                    books.getTotalElements(), location);
            }
            // For category or general search
            else if (searchPattern != null || categoryId != null) {
                log.info("Performing search with filters - search='{}', categoryId={}", search, categoryId);
                if (userId != null) {
                    books = bookRepository.findAllDisplayableBooksWithFilters(
                        pageable, 
                        userId, 
                        null, // location
                        searchPattern, 
                        categoryId
                    );
                } else {
                    books = bookRepository.findAllDisplayableBooksForGuestsWithFilters(
                        pageable, 
                        null, // location
                        searchPattern, 
                        categoryId
                    );
                }
                log.info("Search with filters completed. Found {} books", books.getTotalElements());
            }
            // Default case: get all displayable books
            else {
                log.info("No specific filters provided. Fetching all displayable books");
                if (userId != null) {
                    books = bookRepository.findAllDisplayableBooks(pageable, userId);
                } else {
                    books = bookRepository.findAllDisplayableBooksForGuests(pageable);
                }
                log.info("Fetched {} displayable books", books.getTotalElements());
            }
            
            try {
                List<BookResponse> bookResponses = books.stream()
                    .map(book -> {
                        try {
                            return bookMapper.toBookResponse(book);
                        } catch (Exception e) {
                            log.error("Error mapping book with ID {}: {}", book.getId(), e.getMessage(), e);
                            throw new RuntimeException("Error processing book data", e);
                        }
                    })
                    .toList();
                    
                PageResponse<BookResponse> response = new PageResponse<>(
                    bookResponses,
                    books.getNumber(),
                    books.getSize(),
                    books.getTotalElements(),
                    books.getTotalPages(),
                    books.isFirst(),
                    books.isLast()
                );
                
                log.info("Successfully processed {} books for response", bookResponses.size());
                return response;
                
            } catch (Exception e) {
                log.error("Error processing book list: {}", e.getMessage(), e);
                throw new RuntimeException("Error processing book list: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            log.error("Error in findAllBooks: {}", e.getMessage(), e);
            throw new RuntimeException("Error in findAllBooks: " + e.getMessage(), e);
        }
    }

    //hna anjibo books by owner
    public PageResponse<BookResponse> findAllBooksByOwner(int page, int size, String location, Double lat, Double lng, Double radius, Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Book> books;
        
        log.debug("findAllBooksByOwner called with location='{}', lat={}, lng={}, radius={} for userId={}", 
            location, lat, lng, radius, user.getId());
            
        // If we have coordinates and radius, use them for filtering
        if (lat != null && lng != null && radius != null) {
            log.debug("Using coordinate-based filtering with lat={}, lng={}, radius={}", lat, lng, radius);
            // Convert radius from kilometers to degrees (approximate)
            double radiusInDegrees = radius / 111.0;
            double minLat = lat - radiusInDegrees;
            double maxLat = lat + radiusInDegrees;
            double minLng = lng - (radiusInDegrees / Math.cos(Math.toRadians(lat)));
            double maxLng = lng + (radiusInDegrees / Math.cos(Math.toRadians(lat)));
            
            log.debug("Searching in bounding box: lat=[{}, {}], lng=[{}, {}]", minLat, maxLat, minLng, maxLng);
            
            // First, get all books within the bounding box (fast filter)
            List<Book> booksInBoundingBox = bookRepository.findByOwnerIdAndCoordinatesWithin(
                user.getId(), minLat, maxLat, minLng, maxLng);
                
            log.debug("Found {} books in bounding box", booksInBoundingBox.size());
            
            // Then filter by distance (slower but more accurate)
            List<Book> filteredBooks = booksInBoundingBox.stream()
                .filter(book -> book.getLatitude() != null && book.getLongitude() != null)
                .filter(book -> {
                    double distance = calculateDistance(
                        lat, lng, 
                        book.getLatitude(), book.getLongitude());
                    return distance <= radius;
                })
                .collect(Collectors.toList());
                
            log.debug("After distance filtering: {} books", filteredBooks.size());
            
            // Apply pagination manually
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), filteredBooks.size());
            
            List<Book> paginatedBooks = filteredBooks.subList(start, end);
            List<BookResponse> bookResponses = paginatedBooks.stream()
                .map(bookMapper::toBookResponse)
                .collect(Collectors.toList());
                
            return new PageResponse<>(
                bookResponses,
                page,
                size,
                filteredBooks.size(),
                (int) Math.ceil((double) filteredBooks.size() / size),
                page == 0,
                end >= filteredBooks.size()
            );
        }
        // Fall back to text-based location filtering if no coordinates provided
        else if (location != null && !location.isEmpty()) {
            log.debug("Using text-based location filter: {}", location);
            books = bookRepository.findByOwnerIdAndLocationContainingIgnoreCase(user.getId(), location, pageable);
        } 
        // No filters, just get all books for the owner
        else {
            books = bookRepository.findByOwnerId(user.getId(), pageable);
        }
        
        List<BookResponse> bookResponses = books.stream()
            .map(bookMapper::toBookResponse)
            .collect(Collectors.toList());
            
        return new PageResponse<>(
            bookResponses,
            books.getNumber(),
            books.getSize(),
            books.getTotalElements(),
            books.getTotalPages(),
            books.isFirst(),
            books.isLast()
        );
    }

    //hdi bch njibo all borrowed books li eta l user
    public PageResponse<BorrowedBookResponse> findAllBorrowedBooks(int page, int size, Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        Pageable pageable = (Pageable) PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<BookTransactionHistory> allBorrowedBooks = transactionHistoryRepository.findAllBorrowedBooks((org.springframework.data.domain.Pageable) pageable, user.getId()); //3an tari9 repo li drna jib lia mn BookTransactionHistory user li endo nfs l id libghina (l app fiha bzff user ms hna bghina l book d user whd dyalna li mno anjibo l allborrowed book dyalo)
        List<BorrowedBookResponse> bookResponse = allBorrowedBooks.stream() //BorrowedBookResponse drna fiha data fl bookmapper (hna dir var bookResponse type dyalha BorrowedBookResponse o dir fiha allBorrowedBooks lihia l var li drna fo9 o an3mroha l tht  )
                .map(bookMapper::toBorrowedBookResponse)
                .toList();

        return new PageResponse<>(
                bookResponse,
                allBorrowedBooks.getNumber(),
                allBorrowedBooks.getSize(),
                allBorrowedBooks.getTotalElements(),
                allBorrowedBooks.getTotalPages(),
                allBorrowedBooks.isFirst(),
                allBorrowedBooks.isLast()
        );
    }

    public PageResponse<BorrowedBookResponse> findAllReturnedBooks(int page, int size, Authentication connectedUser) {

        User user = ((User) connectedUser.getPrincipal());
        Pageable pageable = (Pageable) PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<BookTransactionHistory> allBorrowedBooks = transactionHistoryRepository.findAllReturnedBooks((org.springframework.data.domain.Pageable) pageable, user.getId()); //nfs blan l borrow andiro hna f returned drna hna findAllReturnedBooks f repo
        List<BorrowedBookResponse> bookResponse = allBorrowedBooks.stream() //BorrowedBookResponse drna fiha data fl bookmapper (hna dir var bookResponse type dyalha BorrowedBookResponse o dir fiha allBorrowedBooks lihia l var li drna fo9 o an3mroha l tht  )
                .map(bookMapper::toBorrowedBookResponse)
                .toList();

        return new PageResponse<>(
                bookResponse,
                allBorrowedBooks.getNumber(),
                allBorrowedBooks.getSize(),
                allBorrowedBooks.getTotalElements(),
                allBorrowedBooks.getTotalPages(),
                allBorrowedBooks.isFirst(),
                allBorrowedBooks.isLast()
        );
    }

    public Integer updateShareableStatus(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(()-> new EntityNotFoundException("No book found with the ID::" + bookId)); // lmknsh dir hdshi lkn dr lia hdshi li ltht
        User user = ((User) connectedUser.getPrincipal());
        //only owner dl book ymkn ydir lih update
        if (!Objects.equals(book.getOwner().getId(), user.getId())) { //book owner id an9arnoh mea connected user id
            //lmakanosh metsawyin dir lia exeption
            throw new OperationNotPermitedException("You cannot update others books shareable status"); // OperationNotPermittedException dyalna drnaha fl exception file o kola exeption khas ndiro lih handel o drnah wl andiroh f globalexeptionHandler lfile dyalna
        }
        //lkn user connected howa mol ktab
        book.setShareable(!book.isShareable()); //hna kandiro inverse lvalue kn3eksoha bin true o false
        bookRepository.save(book); //moraha dir save
        return bookId; // hit l bookId hia l9ima lmkntsh aydir throw exeption ms lknt aydbl true b false

    }

    public Integer updateArchivedStatus(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(()-> new EntityNotFoundException("No book found with the ID::" + bookId)); // lmknsh dir hdshi lkn dr lia hdshi li ltht
        User user = ((User) connectedUser.getPrincipal());
        //only owner dl book ymkn ydir lih update
        if (!Objects.equals(book.getOwner().getId(), user.getId())) { //book owner id an9arnoh mea connected user id
            //lmakanosh metsawyin dir lia exeption
            throw new OperationNotPermitedException("You cannot update others books archived status"); // OperationNotPermittedException dyalna drnaha fl exception file o kola exeption khas ndiro lih handel o drnah wl andiroh f globalexeptionHandler lfile dyalna
        }
        //lkn user connected howa mol ktab
        book.setArchived(!book.isArchived()); //hna kandiro inverse lvalue kn3eksoha bin true o false
        bookRepository.save(book); //moraha dir save
        return bookId; // hit l bookId hia l9ima lmkntsh aydir throw exeption ms lknt aydbl true b false


    }

    public Integer borrowBook(Integer bookId, Authentication connectedUser) {
        // Find the book by ID
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No book found with the ID: " + bookId));
        
        // Check if book is archived or not shareable
        if (book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermitedException("The requested book cannot be borrowed since it is archived or not shareable");
        }
        
        // Get the authenticated user
        User user = ((User) connectedUser.getPrincipal());
        
        // Check if user is trying to borrow their own book
        if (Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermitedException("You cannot borrow your own book");
        }
        
        // Check if the book is already borrowed by this user and not returned
        final boolean isAlreadyBorrowed = transactionHistoryRepository.isAlreadyBorrowedByUser(bookId, user.getId());
        if (isAlreadyBorrowed) {
            throw new OperationNotPermitedException("The requested book is already borrowed by you");
        }
        
        // Create a new book transaction history record
        BookTransactionHistory bookTransactionHistory = BookTransactionHistory.builder()
                .user(user)
                .book(book)
                .returned(false)
                .returnApproved(false)
                .build();
                
        // Save and return the transaction ID
        return transactionHistoryRepository.save(bookTransactionHistory).getId();
    }
    
    /**
     * Handles returning a borrowed book by a user
     * @param bookId The ID of the book being returned
     * @param connectedUser The authenticated user returning the book
     * @return The ID of the updated transaction
     * @throws EntityNotFoundException if the book is not found
     * @throws OperationNotPermitedException if the user doesn't have permission to return the book
     */
    @Transactional
    public Integer returnBorrowedBook(Integer bookId, Authentication connectedUser) {
        // Get the authenticated user
        User user = ((User) connectedUser.getPrincipal());
        
        // Verify the book exists
        if (!bookRepository.existsById(bookId)) {
            throw new EntityNotFoundException("No book found with ID: " + bookId);
        }
        
        // Find the most recent borrowing transaction for this user and book
        List<BookTransactionHistory> transactions = transactionHistoryRepository
                .findByBookIdAndUserId(bookId, user.getId());
                
        if (transactions.isEmpty()) {
            throw new OperationNotPermitedException("You did not borrow this book");
        }
        
        // Get the most recent transaction
        BookTransactionHistory transaction = transactions.get(0);
        
        // Check if the book is already returned
        if (transaction.isReturned()) {
            throw new OperationNotPermitedException("This book has already been returned");
        }
        
        // Mark the book as returned (but not yet approved)
        transaction.setReturned(true);
        transaction.setReturnApproved(false);
        
        // Save the updated transaction
        return transactionHistoryRepository.save(transaction).getId();
    }



    public Integer approveReturnBorrowBook(Integer bookId, Authentication connectedUser) {
        //nfs blan d returnBorrowedBook

        Book book = bookRepository.findById(bookId)
                .orElseThrow(()-> new EntityNotFoundException("No book found with the ID::" + bookId));

        if (book.isArchived() || !book.isShareable()){
            throw new OperationNotPermitedException("the requested book cannot be borrowed since it is archived or not shareable ");
        }

        User user = ((User) connectedUser.getPrincipal());
        //Only the book owner can approve the return
        if (!Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermitedException("Only the book owner can approve the return");
        }
        //f returnBorrowedBook drrna UserId hna khas ndiro l OwnerId his khas ykon l connected user howa l owner dl book
        List<BookTransactionHistory> transactions = transactionHistoryRepository.findByBookIdAndOwnerId(bookId, user.getId());
        if (transactions.isEmpty()) {
            throw new OperationNotPermitedException("The book is not returned yet. You cannot approve its return ");
        }
        BookTransactionHistory bookTransactionHistory = transactions.get(0); // Get the most recent transaction
        bookTransactionHistory.setReturnApproved(true);
        return transactionHistoryRepository.save(bookTransactionHistory).getId();
    }

    public Book getBookEntityById(Integer bookId) {
        return bookRepository.findById(bookId).orElse(null);
    }

    public void deleteBook(Integer bookId, Authentication connectedUser) {
        Book book = getBookEntityById(bookId);
        User user = ((User) connectedUser.getPrincipal());
        if (!book.getOwner().getId().equals(user.getId())) {
            throw new OperationNotPermitedException("You are not allowed to delete this book");
        }
        bookRepository.delete(book);
    }
    
    /**
     * Calculate distance between two points in kilometers using Haversine formula
     * @param lat1 Latitude of first point
     * @param lon1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lon2 Longitude of second point
     * @return Distance in kilometers
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Convert latitude and longitude from degrees to radians
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // Haversine formula
        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;
        double a = Math.pow(Math.sin(dLat / 2), 2) + 
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.pow(Math.sin(dLon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_KM * c;
    }
}
