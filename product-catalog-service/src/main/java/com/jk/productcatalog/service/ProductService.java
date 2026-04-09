package com.jk.productcatalog.service;

import com.jk.commonlibrary.dto.PaginatedResponse;
import com.jk.productcatalog.dto.request.ProductFilterRequest;
import com.jk.productcatalog.dto.response.ProductListResponse;

public interface ProductService {


    PaginatedResponse<ProductListResponse> getAllProducts(ProductFilterRequest filterRequest);

}
