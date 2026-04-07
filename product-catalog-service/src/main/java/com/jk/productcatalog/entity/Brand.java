package com.jk.productcatalog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Brand entity for product manufacturer/brand management
 * Example: Nike, Adidas, Puma, etc.
 */
@Entity
@Table(
        name = "brands",
        indexes = {
                @Index(name = "idx_brand_slug", columnList = "slug", unique = true),
                @Index(name = "idx_brand_name", columnList = "name"),
                @Index(name = "idx_brand_active", columnList = "isActive")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_brand_slug", columnNames = "slug"),
                @UniqueConstraint(name = "uk_brand_name", columnNames = "name")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String slug;                   // SEO-friendly URL slug (e.g., "nike", "adidas")

    @Column(name = "logo_s3_key", length = 500)
    private String logoS3Key;              // S3 key for brand logo

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;         // Soft delete flag

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    // =========== RELATIONSHIPS ===========

    @OneToMany(mappedBy = "brand", cascade = CascadeType.PERSIST)
    @Builder.Default
    private List<Product> products = new ArrayList<>();


    // =========== HELPER METHODS ===========

    public void addProduct(Product product) {
        if (product != null) {
            products.add(product);
            product.setBrand(this);
        }
    }

    public void removeProduct(Product product) {
        if (product != null) {
            products.remove(product);
            product.setBrand(null);
        }
    }
}
