package com.jk.productcatalog.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTagRequest {

    @NotBlank(message = "Tag name is required")
    @Size(max = 50, message = "Tag name cannot exceed 50 characters")
    private String name;

    @NotBlank(message = "Slug is required")
    @Size(max = 70, message = "Slug cannot exceed 70 characters")
    private String slug;
}
