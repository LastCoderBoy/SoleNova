package com.jk.productcatalog.dto.request;

import com.jk.productcatalog.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name cannot exceed 200 characters")
    private String name;

    @NotBlank(message = "Slug is required")
    @Size(max = 250, message = "Slug cannot exceed 250 characters")
    private String slug;

    private String description;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotNull(message = "Brand ID is required")
    private Long brandId;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull(message = "Base price (cost price) is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    private BigDecimal basePrice; // Maps to costPrice in Product entity

    private Boolean featured;
}
