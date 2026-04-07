package com.jk.productcatalog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Category entity for hierarchical product categorization
 * Supports parent-child relationships for building category trees
 * Example: Men → Classic Shoes, Women → Boots, Women → Heels
 */
@Entity
@Table(
        name = "categories",
        indexes = {
                @Index(name = "idx_category_slug", columnList = "slug", unique = true),
                @Index(name = "idx_category_parent", columnList = "parent_id"),
                @Index(name = "idx_category_active", columnList = "isActive"),
                @Index(name = "idx_category_display_order", columnList = "display_order")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_category_slug", columnNames = "slug")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String slug;                   // SEO-friendly URL slug (e.g., "men-classic-shoes")

    @Column(length = 500)
    private String description;

    @Column(name = "image_s3_key", length = 500)
    private String imageS3Key;             // S3 key for category banner/icon

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;      // Sort order within parent category

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

    // Self-referential relationship for hierarchy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Category> children = new ArrayList<>();

    // Products in this category
    @OneToMany(mappedBy = "category", cascade = CascadeType.PERSIST)
    @Builder.Default
    private List<Product> products = new ArrayList<>();


    // =========== HELPER METHODS ===========

    public void addChild(Category child) {
        if (child != null) {
            children.add(child);
            child.setParent(this);
        }
    }

    public void removeChild(Category child) {
        if (child != null) {
            children.remove(child);
            child.setParent(null);
        }
    }

    public boolean isRootCategory() {
        return parent == null;
    }

    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    public int getLevel() {
        int level = 0;
        Category current = this.parent;
        while (current != null) {
            level++;
            current = current.getParent();
        }
        return level;
    }
}
