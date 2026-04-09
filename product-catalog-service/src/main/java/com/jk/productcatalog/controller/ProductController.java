package com.jk.productcatalog.controller;

import com.jk.commonlibrary.dto.ApiResponse;
import com.jk.commonlibrary.dto.PaginatedResponse;
import com.jk.productcatalog.dto.request.ProductFilterRequest;
import com.jk.productcatalog.dto.response.ProductListResponse;
import com.jk.productcatalog.enums.Gender;
import com.jk.productcatalog.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.jk.commonlibrary.constants.AppConstants.*;
import static com.jk.commonlibrary.constants.AppConstants.DEFAULT_SORT_DIRECTION;
import static com.jk.commonlibrary.constants.AppConstants.DEFAULT_SORT_BY;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(PRODUCT_CATALOG_PATH)
public class ProductController {
    // - `GET /api/v1/products` — List products (pagination, filtering, sorting)
    //  - Query params: `?page=0&size=20&categoryId=1&brandId=2&minPrice=50&maxPrice=200&size=US10&color=Red&tags=casual,summer&sort=price,asc`
    //- `GET /api/v1/products/{slug}` — Get product details by slug
    //- `GET /api/v1/products/{productId}/variants` — Get all variants for a product
    //- `GET /api/v1/products/search?q={query}` — Full-text search

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<ProductListResponse>>> getAllProducts(
            @ModelAttribute @Valid ProductFilterRequest filterRequest){

        log.info("[PRODUCT-CONTROLLER] Get all products - page: {}, size: {}", filterRequest.getPage(), filterRequest.getSize());

        PaginatedResponse<ProductListResponse> products = productService.getAllProducts(filterRequest);
        return ResponseEntity.ok(
                ApiResponse.success("Products retrieved successfully", products)
        );
    }


}
