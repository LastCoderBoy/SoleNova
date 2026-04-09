package com.jk.productcatalog.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageResponse {
    private Long id;
    private String s3Key;
    private String url;
    private String altText;
    private Integer displayOrder;
    private Boolean isPrimary;
}
