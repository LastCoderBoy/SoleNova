package com.jk.productcatalog.dto.request;

import com.jk.productcatalog.enums.Gender;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {

    @Size(max = 200, message = "Product name cannot exceed 200 characters")
    private String name;

    @Size(max = 250, message = "Slug cannot exceed 250 characters")
    private String slug;

    private String description;

    private Gender gender;

    private Long brandId;

    private Long categoryId;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    private BigDecimal basePrice;

    private Boolean featured;

    private Boolean active;
}
