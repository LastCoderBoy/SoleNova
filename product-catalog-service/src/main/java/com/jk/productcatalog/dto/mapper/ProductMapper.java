package com.jk.productcatalog.dto.mapper;

import com.jk.productcatalog.dto.response.ProductListResponse;
import com.jk.productcatalog.entity.Product;

import java.math.BigDecimal;

public class ProductMapper {

    public static ProductListResponse mapToProductListResponse(Product product, String brandName, String categoryName,
                                                               BigDecimal sellingPrice, String primaryImageUrl) {
        return ProductListResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .brandName(brandName)
                .categoryName(categoryName)
                .sellingPrice(sellingPrice)
                .primaryImageUrl(primaryImageUrl)
                .featured(product.getIsFeatured())
                .inStock(product.isInStock())
                .build();
    }
}
