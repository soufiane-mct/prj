package com.aliboo.book.book;

import com.aliboo.book.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(
    value = "/books",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Book Images")
@RequiredArgsConstructor
@CrossOrigin(
    origins = {
        "http://localhost:4200",
        "http://127.0.0.1:4200"
    },
    allowedHeaders = "*",
    allowCredentials = "true"
)
@ControllerAdvice
/**
 * Controller for handling book image operations.
 * Note: Make sure this controller is in a package that's scanned by Spring's component scan.
 */
public class BookImageController {
    private final BookImageService bookImageService;
    
    @Value("${application.file.upload.photos-output-path}")
    private String uploadPath;

    @Value("${application.base-url:http://localhost:8081}")
    private String baseUrl;

    @Operation(summary = "Upload multiple images for a book")
    @PostMapping(
        value = "/{bookId}/images/upload",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> uploadBookImages(
            @PathVariable Integer bookId,
            @RequestParam("files") MultipartFile[] filesArray,
            @RequestParam(value = "firstAsCover", required = false, defaultValue = "true") boolean firstAsCover,
            Authentication authentication) {
        
        log.info("=== UPLOAD IMAGES REQUEST FOR BOOK ID: {} ===", bookId);
        
        if (filesArray == null || filesArray.length == 0) {
            log.warn("No files provided in the request");
            return ResponseEntity.badRequest().body("No files provided");
        }

        try {
            // Convert array to list for service method
            List<MultipartFile> files = new ArrayList<>();
            for (MultipartFile file : filesArray) {
                if (file != null && !file.isEmpty()) {
                    files.add(file);
                }
            }
            
            log.info("Number of valid files to process: {}", files.size());
            
            // Log authentication details
            log.debug("Authentication details: {}", authentication);
            if (authentication == null || !authentication.isAuthenticated()) {
                log.error("User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
            }

            User user = (User) authentication.getPrincipal();
            log.info("Uploading {} files for book ID: {}, user ID: {}", files.size(), bookId, user.getId());
            
            // Log file details
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                log.debug("Processing file {}: Name: {}, Size: {} bytes, Content Type: {}", 
                    i + 1,
                    file.getOriginalFilename(), 
                    file.getSize(),
                    file.getContentType());
            }
            
            List<BookImage> savedImages = bookImageService.uploadBookImages(
                bookId, 
                files, 
                firstAsCover, 
                user.getId()
            );
            
            log.info("Successfully processed {} images for book ID: {}", savedImages.size(), bookId);
            
            // Convert file paths to accessible URLs
            List<Map<String, Object>> imageResponses = savedImages.stream()
                .map(img -> {
                    Map<String, Object> imgMap = new HashMap<>();
                    imgMap.put("id", img.getId());
                    imgMap.put("isCover", img.isCover());
                    // Convert file path to URL
                    String filePath = img.getImageUrl();
                    String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
                    String imageUrl = String.format("/api/v1/books/%d/images/%s", bookId, fileName);
                    imgMap.put("imageUrl", imageUrl);
                    return imgMap;
                })
                .collect(Collectors.toList());
            
            // Create a proper response object
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Successfully uploaded " + savedImages.size() + " images");
            response.put("images", imageResponses);
            
            return ResponseEntity.ok(response);
            
        } catch (ClassCastException e) {
            log.error("Invalid user principal type", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Authentication error: Invalid user principal type");
        } catch (Exception e) {
            log.error("Error uploading images for book ID: {}", bookId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error uploading images: " + e.getMessage());
        }
    }

    @Operation(summary = "Get all images for a book")
    @GetMapping("/{bookId}/images")
    public ResponseEntity<List<Map<String, Object>>> getBookImages(@PathVariable Integer bookId) {
        log.info("Fetching all images for book ID: {}", bookId);
        List<BookImage> images = bookImageService.getBookImages(bookId);
        
        List<Map<String, Object>> response = images.stream()
            .map(image -> {
                Map<String, Object> imgMap = new HashMap<>();
                imgMap.put("id", image.getId());
                // Return the full URL to the image
                imgMap.put("imageUrl", baseUrl + "/api/v1/books/" + bookId + "/images/" + 
                    image.getImageUrl().substring(image.getImageUrl().lastIndexOf('/') + 1));
                imgMap.put("isCover", image.isCover());
                return imgMap;
            })
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Get a book cover image by filename")
    @GetMapping(value = "/{bookId}/images/{filename:.+}", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_GIF_VALUE})
    public ResponseEntity<Resource> getBookCoverImage(
            @PathVariable Integer bookId, 
            @PathVariable String filename) throws IOException {
        
        log.info("Fetching cover image: {} for book ID: {}", filename, bookId);
        
        try {
            // Get the base uploads directory
            Path uploadsDir = Paths.get(uploadPath).toAbsolutePath().normalize();
            log.info("Searching in uploads directory: {}", uploadsDir);
            
            // Search for the file in all user directories
            Path booksDir = uploadsDir.resolve("books");
            if (!Files.exists(booksDir)) {
                log.error("Books directory not found: {}", booksDir);
                return ResponseEntity.notFound().build();
            }
            
            // Look for the file in any user's book directory
            Path foundFile = null;
            try (var userDirs = Files.list(booksDir)) {
                for (Path userDir : userDirs.collect(Collectors.toList())) {
                    if (Files.isDirectory(userDir) && userDir.getFileName().toString().startsWith("user_")) {
                        Path bookDir = userDir.resolve("book_" + bookId);
                        if (Files.exists(bookDir)) {
                            Path imageFile = bookDir.resolve(filename);
                            if (Files.exists(imageFile)) {
                                foundFile = imageFile;
                                break;
                            }
                        }
                    }
                }
            }
            
            if (foundFile == null) {
                log.error("Image not found: {} for book ID: {}", filename, bookId);
                return ResponseEntity.notFound().build();
            }
            
            log.info("Found image at: {}", foundFile);
            Resource resource = new UrlResource(foundFile.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
            } else {
                log.error("Could not read file: {}", foundFile);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error loading image: {}", filename, e);
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "List all book images (for debugging)")
    @GetMapping("/images/all")
    public ResponseEntity<List<BookImage>> getAllBookImages() {
        log.info("Fetching all book images");
        try {
            List<BookImage> images = bookImageService.getAllBookImages();
            log.info("Found {} book images in total", images.size());
            return ResponseEntity.ok(images);
        } catch (Exception e) {
            log.error("Error fetching all book images", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @DeleteMapping("/{bookId}/images/{imageId}")
    public ResponseEntity<Void> deleteBookImage(
            @PathVariable Integer bookId,
            @PathVariable Long imageId) {
        bookImageService.deleteBookImage(bookId, imageId);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/{bookId}/images")
    public ResponseEntity<Void> deleteAllBookImages(@PathVariable Integer bookId) {
        bookImageService.deleteAllBookImages(bookId);
        return ResponseEntity.noContent().build();
    }
}
