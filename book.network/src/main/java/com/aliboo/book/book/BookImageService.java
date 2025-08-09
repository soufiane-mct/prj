package com.aliboo.book.book;

import com.aliboo.book.file.FileStorageService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookImageService {
    private final BookImageRepository bookImageRepository;
    private final BookRepository bookRepository;
    private final FileStorageService storageService;

    @Transactional
    public List<BookImage> uploadBookImages(Integer bookId, List<MultipartFile> files, boolean firstAsCover, Integer userId) {
        log.info("=== STARTING IMAGE UPLOAD ===");
        log.info("Book ID: {}", bookId);
        log.info("User ID: {}", userId);
        log.info("First as cover: {}", firstAsCover);
        log.info("Number of files to process: {}", files != null ? files.size() : 0);
        
        if (files == null || files.isEmpty()) {
            log.error("No files provided for upload");
            throw new IllegalArgumentException("No files provided for upload");
        }
        
        // Log details of each file
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            if (file == null) {
                log.warn("File at index {} is null", i);
                continue;
            }
            log.info("File {}: Name={}, Size={} bytes, ContentType={}", 
                i + 1, 
                file.getOriginalFilename(),
                file.getSize(),
                file.getContentType());
        }
        
        // Verify the book exists and the user has permission
        log.info("Looking up book with ID: {}", bookId);
        var book = bookRepository.findById(bookId)
                .orElseThrow(() -> {
                    String errorMsg = String.format("Book not found with id: %d", bookId);
                    log.error(errorMsg);
                    return new EntityNotFoundException(errorMsg);
                });
                
        log.info("Found book: ID={}, Title={}, Owner ID={}", 
            book.getId(), book.getTitle(), 
            book.getOwner() != null ? book.getOwner().getId() : "null");
                
        // Verify the user has permission to upload images for this book
        if (book.getOwner() == null || !book.getOwner().getId().equals(userId)) {
            String errorMsg = String.format("User %d is not authorized to upload images for book %d", userId, bookId);
            log.warn(errorMsg);
            throw new SecurityException("You are not authorized to upload images for this book");
        }

        List<BookImage> savedImages = new ArrayList<>();
        
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            if (file == null || file.isEmpty()) {
                log.warn("Skipping empty or null file at index {} for book ID: {}", i, bookId);
                continue;
            }
            
            try {
                // Create a clean subdirectory path
                String bookSubPath = String.format("books/user_%d/book_%d", userId, bookId);
                log.info("Uploading file to subdirectory: {}", bookSubPath);
                log.info("File details - Name: {}, Size: {} bytes, Content-Type: {}", 
                    file.getOriginalFilename(), 
                    file.getSize(),
                    file.getContentType());
                
                String imageUrl;
                try {
                    imageUrl = storageService.uploadFile(file, bookSubPath);
                    log.info("File uploaded successfully. URL: {}", imageUrl);
                    
                    if (imageUrl == null || imageUrl.trim().isEmpty()) {
                        log.error("Upload succeeded but returned null or empty URL for file: {}", 
                            file.getOriginalFilename());
                        continue; // Skip this file but continue with others
                    }
                } catch (Exception e) {
                    log.error("Error uploading file {}: {}", file.getOriginalFilename(), e.getMessage(), e);
                    continue; // Skip to next file on upload error
                }
                
                // Handle cover image logic
                boolean isCover = firstAsCover && i == 0;
                log.info("File {} will {}be set as cover", 
                    file.getOriginalFilename(), 
                    isCover ? "" : "not ");
                
                try {
                    // If we're setting a new cover, unset any existing cover
                    if (isCover) {
                        log.info("Unsetting existing covers for book ID: {}", bookId);
                        int unsetCount = bookImageRepository.unsetExistingCovers(bookId);
                        log.info("Unset {} existing covers for book ID: {}", unsetCount, bookId);
                    }
                    
                    // Create and save the book image
                    BookImage bookImage = BookImage.builder()
                            .imageUrl(imageUrl)
                            .isCover(isCover)
                            .book(book)
                            .build();
                    
                    log.info("Saving BookImage entity to database - URL: {}, IsCover: {}", 
                        imageUrl, isCover);
                        
                    BookImage savedImage = bookImageRepository.save(bookImage);
                    log.info("Successfully saved BookImage with ID: {}", savedImage.getId());
                    
                    savedImages.add(savedImage);
                    log.info("Successfully processed image {} for book ID: {}", 
                        file.getOriginalFilename(), bookId);
                } catch (Exception e) {
                    log.error("Error processing file {}: {}", file.getOriginalFilename(), e.getMessage(), e);
                    // Continue with next file even if one fails
                }
                    
            } catch (Exception e) {
                String errorMsg = String.format("Error processing file %s: %s", 
                    file != null ? file.getOriginalFilename() : "unknown",
                    e.getMessage());
                log.error(errorMsg, e);
                // Continue with next file even if one fails
            }
        }
        
        if (savedImages.isEmpty() && !files.isEmpty()) {
            log.error("Failed to upload any images for book ID: {}", bookId);
            throw new RuntimeException("Failed to upload any images. Please check server logs for details.");
        }
        
        return savedImages;
    }

    @Transactional(readOnly = true)
    public List<BookImage> getBookImages(Integer bookId) {
        log.info("Fetching all images for book ID: {}", bookId);
        return bookImageRepository.findByBookId(bookId);
    }
    
    /**
     * Get all book image URLs for a specific book
     * @param bookId The ID of the book
     * @return List of image URLs
     */
    public List<String> getBookImageUrls(Integer bookId) {
        log.info("Fetching image URLs for book ID: {}", bookId);
        List<BookImage> images = bookImageRepository.findByBookId(bookId);
        List<String> urls = new ArrayList<>();
        
        for (BookImage image : images) {
            urls.add(image.getImageUrl());
        }
        
        log.debug("Found {} image URLs for book ID: {}", urls.size(), bookId);
        return urls;
    }
    
    /**
     * Get all book images (for debugging purposes)
     * @return List of all book images in the system
     */
    public List<BookImage> getAllBookImages() {
        log.info("Fetching all book images from the database");
        List<BookImage> images = bookImageRepository.findAll();
        log.info("Found {} book images in total", images.size());
        return images;
    }

    @Transactional(readOnly = true)
    public List<BookImage> getBookImagesOrderByCoverAndId(Integer bookId) {
        return bookImageRepository.findByBookIdOrderByIsCoverDescIdAsc(bookId);
    }

    @Transactional
    public void deleteBookImage(Integer bookId, Long imageId) {
        // Verify the image belongs to the specified book before deleting
        bookImageRepository.findById(imageId).ifPresent(bookImage -> {
            if (!bookImage.getBook().getId().equals(bookId)) {
                throw new EntityNotFoundException("Image not found for the specified book");
            }
            bookImageRepository.deleteById(imageId);
        });
    }
    
    @Transactional
    public void deleteAllBookImages(Integer bookId) {
        bookImageRepository.deleteByBookId(bookId);
    }
}
