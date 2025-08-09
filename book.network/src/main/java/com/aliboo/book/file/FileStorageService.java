package com.aliboo.book.file;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

import jakarta.annotation.PostConstruct;

import static java.io.File.separator;

@Service
@Slf4j //hdi annotation bch nkhdmo b log (bsh ntl3o err ltht ra f if statement)
@RequiredArgsConstructor
public class FileStorageService {

    @Value("${application.file.upload.photos-output-path}")
    private String fileUploadPath;
    
    @PostConstruct
    public void init() {
        log.info("=== FILE STORAGE SERVICE INITIALIZATION ===");
        log.info("FileStorageService initialized with upload path: {}", fileUploadPath);
        
        // Convert to absolute path for better logging
        File uploadDir = new File(fileUploadPath).getAbsoluteFile();
        log.info("Absolute upload path: {}", uploadDir.getAbsolutePath());
        
        // Check directory existence and permissions
        log.info("Upload directory exists: {}", uploadDir.exists());
        if (uploadDir.exists()) {
            log.info("Upload directory is directory: {}", uploadDir.isDirectory());
            log.info("Upload directory is readable: {}", uploadDir.canRead());
            log.info("Upload directory is writable: {}", uploadDir.canWrite());
            log.info("Upload directory is executable: {}", uploadDir.canExecute());
            
            // List contents of the upload directory for debugging
            File[] files = uploadDir.listFiles();
            if (files != null) {
                log.info("Upload directory contains {} items", files.length);
                for (File file : files) {
                    log.info(" - {} ({})", file.getName(), 
                        file.isDirectory() ? "directory" : "file");
                }
            } else {
                log.warn("Could not list contents of upload directory");
            }
        } else {
            log.warn("Upload directory does not exist. Attempting to create...");
            if (uploadDir.mkdirs()) {
                log.info("Successfully created upload directory: {}", uploadDir.getAbsolutePath());
            } else {
                log.error("Failed to create upload directory: {}", uploadDir.getAbsolutePath());
            }
        }
    }

    public String saveFile(@Nonnull MultipartFile sourceFile,@Nonnull Integer userId) {
        final String fileUploadSubPath = "users" + separator + userId ; //separator bch bla mnhtmo b details dl files wsh - wl _...

        return uploadFile(sourceFile, fileUploadSubPath);
    }

    /**
     * Uploads a file and returns the relative path
     * @param sourceFile The file to upload
     * @param fileUploadSubPath The subdirectory path (relative to the base upload directory)
     * @return The relative path of the uploaded file (e.g., 'books/user_123/book_456/videos/filename.ext')
     */
    public String uploadFile(@Nonnull MultipartFile sourceFile, @Nonnull String fileUploadSubPath) {
        String originalFilename = sourceFile.getOriginalFilename();
        long fileSize = sourceFile.getSize();
        String contentType = sourceFile.getContentType();
        
        log.info("=== STARTING FILE UPLOAD ===");
        log.info("Original filename: {}", originalFilename);
        log.info("File size: {} bytes", fileSize);
        log.info("Content type: {}", contentType);
        log.info("Upload subpath: {}", fileUploadSubPath);
        
        try {
            // Validate input file
            if (sourceFile.isEmpty()) {
                log.error("Upload failed: File is empty");
                return null;
            }
            
            // Normalize the path to use forward slashes consistently
            String normalizedPath = fileUploadSubPath.replace("\\", "/");
            log.info("Normalized upload subpath: {}", normalizedPath);
            
            // Ensure the base upload path exists and is a directory
            File baseDir = new File(fileUploadPath).getAbsoluteFile();
            log.info("Base upload directory: {}", baseDir.getAbsolutePath());
            
            // Check base directory permissions
            if (!baseDir.exists()) {
                log.warn("Base upload directory does not exist. Attempting to create...");
                if (!baseDir.mkdirs()) {
                    log.error("Failed to create base upload directory: {}", baseDir.getAbsolutePath());
                    return null;
                }
                log.info("Successfully created base upload directory");
            } else if (!baseDir.isDirectory()) {
                log.error("Upload path exists but is not a directory: {}", baseDir.getAbsolutePath());
                return null;
            } else if (!baseDir.canWrite()) {
                log.error("No write permissions for upload directory: {}", baseDir.getAbsolutePath());
                return null;
            }
            
            // Create the full upload path
            String finalUploadPath = fileUploadPath + "/" + normalizedPath;
            log.info("Final upload path: {}", finalUploadPath);
            
            File targetFolder = new File(finalUploadPath).getAbsoluteFile();
            log.info("Absolute target folder: {}", targetFolder.getAbsolutePath());
            
            // Create the target directory if it doesn't exist
            if (!targetFolder.exists()) {
                log.info("Creating target directory: {}", targetFolder.getAbsolutePath());
                if (!targetFolder.mkdirs()) {
                    log.error("Failed to create target directory: {}", targetFolder.getAbsolutePath());
                    return null;
                }
                log.info("Successfully created target directory");
            } else if (!targetFolder.isDirectory()) {
                log.error("Target path exists but is not a directory: {}", targetFolder.getAbsolutePath());
                return null;
            } else if (!targetFolder.canWrite()) {
                log.error("No write permissions for target directory: {}", targetFolder.getAbsolutePath());
                return null;
            }
            // Create a unique filename with timestamp and original extension
            String safeFilename = originalFilename != null ? 
                originalFilename.replaceAll("[^a-zA-Z0-9.\\-]", "_") : "uploaded_file";
            
            String filename = System.currentTimeMillis() + "_" + safeFilename;
            String targetFilePath = finalUploadPath + "/" + filename;
            
            // Ensure the full path is valid and normalized
            targetFilePath = targetFilePath.replace("\\", "/");
            log.info("Target file path: {}", targetFilePath);
            
            // Get the target file object
            File targetFile = new File(targetFilePath).getAbsoluteFile();
            log.info("Absolute target file path: {}", targetFile.getAbsolutePath());
            
            // Check if file already exists and generate a new name if needed
            if (targetFile.exists()) {
                log.info("File already exists, generating new filename...");
                String baseName = filename.substring(0, filename.lastIndexOf('.'));
                String extension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
                }
                filename = baseName + "_" + (int)(Math.random() * 10000) + extension;
                targetFilePath = finalUploadPath + "/" + filename;
                targetFile = new File(targetFilePath).getAbsoluteFile();
                log.info("Using new filename to avoid conflict: {}", targetFile.getName());
            }
            
            try {
                log.info("Attempting to save file to: {}", targetFile.getAbsolutePath());
                
                // Save the file
                sourceFile.transferTo(targetFile);
                
                // Verify the file was written
                if (!targetFile.exists() || targetFile.length() == 0) {
                    log.error("Failed to save file - file does not exist or is empty after write");
                    return null;
                }
                
                log.info("Successfully saved file ({} bytes) to: {}", 
                    targetFile.length(), targetFile.getAbsolutePath());
                
                // Return the relative path (without the base upload path)
                String relativePath = fileUploadSubPath + "/" + filename;
                // Normalize path to use forward slashes
                return relativePath.replace("\\", "/");
            } catch (IOException e) {
                log.error("I/O Error saving file to: " + targetFile.getAbsolutePath(), e);
                log.error("Error details: {}", e.getMessage());
                
                // Additional debug info
                log.error("File exists: {}", targetFile.exists());
                log.error("File is directory: {}", targetFile.isDirectory());
                log.error("Parent directory exists: {}", targetFile.getParentFile().exists());
                log.error("Parent directory is writable: {}", targetFile.getParentFile().canWrite());
                
                return null;
            }
        } catch (Exception e) {
            log.error("Unexpected error during file upload", e);
            log.error("Error details: {}", e.getMessage());
            
            // Log the full stack trace for debugging
            log.error("Stack trace:", e);
            
            return null;
        } finally {
            log.info("=== FILE UPLOAD COMPLETED ===");
        }
    }
}
