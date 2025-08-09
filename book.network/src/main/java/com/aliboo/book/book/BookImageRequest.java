package com.aliboo.book.book;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class BookImageRequest {
    private MultipartFile file;
    private boolean isCover;
}
