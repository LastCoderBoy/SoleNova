package com.jk.productcatalog.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariantResponse {
    private Long id;
    private String sku;
    private String size;
    private String color;
    private String colorHex;
    private BigDecimal price; // retailPrice
    private BigDecimal salePrice;
    private Integer stockQuantity;
    private List<ImageResponse> images;
    private Boolean active;
}
