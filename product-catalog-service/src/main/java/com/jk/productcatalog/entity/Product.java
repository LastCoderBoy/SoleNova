package com.jk.productcatalog.entity;

import com.jk.productcatalog.enums.Gender;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Product entity representing base product information
 * A product can have multiple variants (different sizes, colors, etc.)
 */
@Entity
@Table(
        name = "products",
        indexes = {
                @Index(name = "idx_product_slug", columnList = "slug", unique = true),
                @Index(name = "idx_product_name", columnList = "name"),
                @Index(name = "idx_product_brand", columnList = "brand_id"),
                @Index(name = "idx_product_category", columnList = "category_id"),
                @Index(name = "idx_product_active_featured", columnList = "isActive, featured"),
                @Index(name = "idx_product_gender", columnList = "gender")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_product_slug", columnNames = "slug")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, unique = true, length = 250)
    private String slug;                   // SEO-friendly URL slug

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;                 // MALE, FEMALE, UNISEX

    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;  // Wholesale/supplier cost (internal, not exposed to customers)

    @Column(nullable = false)
    @Builder.Default
    private Boolean isFeatured = false;      // Featured on homepage/promotions

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;         // Soft delete flag

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    // =========== RELATIONSHIPS ===========

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductVariant> variants = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "product_tags",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"),
            indexes = {
                    @Index(name = "idx_product_tags_product", columnList = "product_id"),
                    @Index(name = "idx_product_tags_tag", columnList = "tag_id")
            }
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();


    // =========== HELPER METHODS ===========

    public void addVariant(ProductVariant variant) {
        if (variant != null) {
            variants.add(variant);
            variant.setProduct(this);
        }
    }

    public void removeVariant(ProductVariant variant) {
        if (variant != null) {
            variants.remove(variant);
            variant.setProduct(null);
        }
    }

    public void addImage(ProductImage image) {
        if (image != null) {
            images.add(image);
            image.setProduct(this);
        }
    }

    public void removeImage(ProductImage image) {
        if (image != null) {
            images.remove(image);
            image.setProduct(null);
        }
    }

    public void addTag(Tag tag) {
        if (tag != null) {
            tags.add(tag);
            tag.getProducts().add(this);
        }
    }

    public void removeTag(Tag tag) {
        if (tag != null) {
            tags.remove(tag);
            tag.getProducts().remove(this);
        }
    }

    public boolean hasVariants() {
        return variants != null && !variants.isEmpty();
    }

    public boolean hasImages() {
        return images != null && !images.isEmpty();
    }

    public boolean isInStock() {
        return variants.stream()
                .anyMatch(variant -> variant.getIsActive() && variant.getStockQuantity() > 0);
    }

    public ProductImage getPrimaryImage() {
        return images.stream()
                .filter(ProductImage::getIsPrimary)
                .findFirst()
                .orElse(images.isEmpty() ? null : images.get(0));
    }
}
