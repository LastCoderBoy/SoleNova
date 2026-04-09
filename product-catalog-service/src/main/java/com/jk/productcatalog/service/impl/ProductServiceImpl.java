package com.jk.productcatalog.service.impl;

import com.jk.commonlibrary.dto.PaginatedResponse;
import com.jk.productcatalog.dto.mapper.ProductMapper;
import com.jk.productcatalog.dto.request.ProductFilterRequest;
import com.jk.productcatalog.dto.response.ProductListResponse;
import com.jk.productcatalog.entity.Product;
import com.jk.productcatalog.entity.ProductImage;
import com.jk.productcatalog.repository.ProductRepository;
import com.jk.productcatalog.service.ProductService;
import com.jk.productcatalog.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static javax.management.Query.and;


@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    @Override
    public PaginatedResponse<ProductListResponse> getAllProducts(ProductFilterRequest filterRequest) {

        Specification<Product> spec = Specification
                .where(ProductSpecification.hasSearch(filterRequest.getSearch()))
                .and(ProductSpecification.hasCategoryId(filterRequest.getCategoryId()))
                .and(ProductSpecification.hasBrandId(filterRequest.getBrandId()))
                .and(ProductSpecification.hasColors(filterRequest.getColors()))
                .and(ProductSpecification.hasTags(filterRequest.getTags()));

        Sort sort = filterRequest.getSortDirection().equalsIgnoreCase("desc")
                ? Sort.by(filterRequest.getSortBy()).descending()
                : Sort.by(filterRequest.getSortBy()).ascending();

        Pageable pageable = PageRequest.of(filterRequest.getPage(), filterRequest.getSize(), sort);

        Page<Product> productPage = productRepository.findAll(spec, pageable);
        productPage.getContent().forEach(product -> {
            StringBuilder categoryName = new StringBuilder();
            StringBuilder brandName = new StringBuilder();
            if (product.getCategory() != null) {
                categoryName.append(product.getCategory().getName());
            }
            if (product.getBrand() != null) {
                brandName.append(product.getBrand().getName());
            }
            String primaryImageUrl = product.getPrimaryImage().getUrl();
            BigDecimal effectivePrice = product.getEffectivePrice();

            ProductMapper.mapToProductListResponse(
                    product,
                    brandName.toString(),
                    categoryName.toString(),
                    effectivePrice,
                    primaryImageUrl);
        });


        log.info("[PRODUCT-SERVICE] Products fetched | found={}, totalPages={}, page={}, size={}",
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                filterRequest.getPage(),
                filterRequest.getSize());

        return null;
    }
}
