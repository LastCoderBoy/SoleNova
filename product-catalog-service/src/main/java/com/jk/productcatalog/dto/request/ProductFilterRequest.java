package com.jk.productcatalog.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

import static com.jk.commonlibrary.constants.AppConstants.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFilterRequest {

    @Builder.Default
    @PositiveOrZero
    private int page = DEFAULT_PAGE_NUMBER;

    @Builder.Default
    @Positive
    @Max(100)
    private int size = DEFAULT_PRODUCT_SIZE;

    @Builder.Default
    @Pattern(regexp = "name|effectivePrice|slug", message = "Invalid sort field. Must be either 'name', 'effectivePrice', or 'slug'")
    private String sortBy = DEFAULT_SORT_BY;

    @Builder.Default
    @Pattern(regexp = "asc|desc", message = "Invalid sort direction. Must be either 'asc' or 'desc'")
    private String sortDirection = DEFAULT_SORT_DIRECTION;

    @Size(max = 100, message = "Search term must not exceed 100 characters")
    private String search;

    @DecimalMin("0.0")
    private BigDecimal minPrice;

    @DecimalMin("0.0")
    private BigDecimal maxPrice;

    private Long categoryId;

    private Long brandId;

    private List<String> colors;

    private List<String> tags;

    @Builder.Default
    private Boolean isInStock = true ;    // Show only products with available stock
}
