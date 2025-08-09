package com.aliboo.book.book;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/books")
public class VideoController {
    private static final String UPLOAD_BASE_PATH = System.getProperty("user.dir") + "/uploads";

    @GetMapping("/{bookId}/videos/{filename:.+}")
    public ResponseEntity<Resource> streamVideo(
            @PathVariable Integer bookId,
            @PathVariable String filename,
            HttpServletRequest request) {
        
        log.info("=== VIDEO REQUEST START ===");
        log.info("Received request for bookId: {}, filename: {}", bookId, filename);
        log.info("Request URL: {}", request.getRequestURL());
        log.info("Request URI: {}", request.getRequestURI());
        log.info("Context Path: {}", request.getContextPath());
        log.info("Servlet Path: {}", request.getServletPath());
        log.info("Path Info: {}", request.getPathInfo());
        log.info("Base upload path: {}", UPLOAD_BASE_PATH);
        
        try {
            // First, find the actual user directory
            File uploadsDir = new File(UPLOAD_BASE_PATH, "books");
            log.info("Looking for videos in directory: {}", uploadsDir.getAbsolutePath());
            
            if (!uploadsDir.exists() || !uploadsDir.isDirectory()) {
                log.error("Uploads directory does not exist: {}", uploadsDir.getAbsolutePath());
                throw new ResponseStatusException(NOT_FOUND, "Videos directory not found");
            }
            
            File[] userDirs = uploadsDir.listFiles(File::isDirectory);
            if (userDirs == null || userDirs.length == 0) {
                log.error("No user directories found in {}", uploadsDir.getAbsolutePath());
                throw new ResponseStatusException(NOT_FOUND, "No user directories found");
            }
            
            // Look for the book in any user's directory
            Path filePath = null;
            for (File userDir : userDirs) {
                File bookDir = new File(userDir, "book_" + bookId);
                if (bookDir.exists()) {
                    File videoFile = new File(new File(bookDir, "videos"), filename);
                    log.info("Checking for video at: {}", videoFile.getAbsolutePath());
                    if (videoFile.exists() && videoFile.isFile()) {
                        filePath = videoFile.toPath();
                        log.info("Found video at: {}", filePath);
                        break;
                    }
                }
            }
            
            if (filePath == null) {
                log.error("Video file not found for book {} and filename {}", bookId, filename);
                throw new ResponseStatusException(NOT_FOUND, "Video not found");
            }
            
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                log.error("Video file exists but cannot be read: {}", filePath);
                throw new ResponseStatusException(NOT_FOUND, "Video exists but cannot be read");
            }
            
            String contentType = determineContentType(filename);
            log.info("Serving video file: {} with content type: {}", filePath, contentType);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .contentLength(Files.size(filePath))
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           String.format("inline; filename=\"%s\"", filename))
                    .body(resource);
                    
        } catch (IOException e) {
            log.error("Error reading video file: {}", e.getMessage(), e);
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Error reading video file", e);
        } catch (Exception e) {
            log.error("Error streaming video: {}", e.getMessage(), e);
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Error streaming video", e);
        }
    }
    
    private String determineContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        return switch (extension) {
            case "mp4" -> "video/mp4";
            case "webm" -> "video/webm";
            case "ogg" -> "video/ogg";
            default -> "application/octet-stream";
        };
    }
}
