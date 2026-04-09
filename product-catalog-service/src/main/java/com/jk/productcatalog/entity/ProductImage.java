package com.jk.productcatalog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * ProductImage entity for managing product and variant-specific images
 * Supports both product-level images (shared across variants) and variant-specific images
 * Images are stored in AWS S3
 */
@Entity
@Table(
        name = "product_images",
        indexes = {
                @Index(name = "idx_image_product_id", columnList = "product_id"),
                @Index(name = "idx_image_variant_id", columnList = "variant_id"),
                @Index(name = "idx_image_primary", columnList = "is_primary"),
                @Index(name = "idx_image_display_order", columnList = "display_order")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "s3_key", nullable = false, length = 500)
    private String s3Key;                 // S3 object key — e.g. "products/123/uuid.jpg"

    @Column(nullable = false, length = 1000)
    private String url;                   // Full S3 URL for serving images

    @Column(name = "alt_text", length = 255)
    private String altText;               // Accessibility + SEO

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;     // Sort order for displaying images

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = false;    // Only one primary image per product

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


    // =========== RELATIONSHIPS ===========

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Nullable — if null, this is a product-level image
    // If non-null, this is a variant-specific image
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;


    // =========== HELPER METHODS ===========

    public boolean isProductLevelImage() {
        return variant == null;
    }

    public boolean isVariantLevelImage() {
        return variant != null;
    }

}
