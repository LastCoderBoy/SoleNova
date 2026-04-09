package com.jk.productcatalog.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductListResponse {
    private Long id;
    private String name;
    private String slug;
    private String brandName;
    private String categoryName;
    private BigDecimal sellingPrice; // isOnSale() ? salePrice : retailPrice
    private String primaryImageUrl;
    private Boolean featured;
    private Boolean inStock;
}
