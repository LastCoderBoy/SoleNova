package com.jk.productcatalog.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadRequest {

    @NotNull(message = "File is required")
    private MultipartFile file;

    private String altText;

    private Integer displayOrder;

    private Boolean isPrimary;
}
