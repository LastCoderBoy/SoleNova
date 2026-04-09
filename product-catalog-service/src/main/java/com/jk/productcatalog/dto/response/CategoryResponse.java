package com.jk.productcatalog.dto.response;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String imageUrl;
    private List<CategoryResponse> children;
    private Long productCount;
    private Boolean active;
}
