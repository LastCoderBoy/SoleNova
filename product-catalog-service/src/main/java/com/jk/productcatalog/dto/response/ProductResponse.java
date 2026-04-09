package com.jk.productcatalog.dto.response;

import com.jk.productcatalog.enums.Gender;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private Gender gender;
    private BrandResponse brand;
    private CategoryResponse category;
    private BigDecimal basePrice; // costPrice
    private List<VariantResponse> variants;
    private List<ImageResponse> images;
    private List<TagResponse> tags;
    private Boolean featured;
    private Boolean active;
}
