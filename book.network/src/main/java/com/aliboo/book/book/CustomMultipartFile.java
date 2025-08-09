package com.aliboo.book.book;

import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class CustomMultipartFile implements MultipartFile {
    private final byte[] content;
    private final String name;
    private final String originalFilename;
    private final String contentType;
    private final long size;

    public CustomMultipartFile(byte[] content, String originalFilename, String contentType) {
        this.content = content != null ? content : new byte[0];
        this.originalFilename = originalFilename != null ? originalFilename : "";
        this.name = this.originalFilename;
        this.contentType = contentType != null ? contentType : "application/octet-stream";
        this.size = this.content.length;
    }

    @Override
    @NonNull
    public String getName() {
        return this.name;
    }

    @Override
    @NonNull
    public String getOriginalFilename() {
        return this.originalFilename;
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public long getSize() {
        return this.size;
    }

    @Override
    @NonNull
    public byte[] getBytes() throws IOException {
        return this.content;
    }

    @Override
    @NonNull
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(this.content);
    }

    @Override
    public void transferTo(@NonNull java.io.File dest) throws IOException, IllegalStateException {
        Objects.requireNonNull(dest, "Destination file must not be null");
        java.nio.file.Files.write(dest.toPath(), this.content);
    }
}
