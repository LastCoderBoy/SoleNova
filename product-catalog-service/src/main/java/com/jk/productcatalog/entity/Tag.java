package com.jk.productcatalog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Tag entity for flexible product categorization and filtering
 * Example: casual, formal, summer, waterproof, sports, etc.
 */
@Entity
@Table(
        name = "tags",
        indexes = {
                @Index(name = "idx_tag_slug", columnList = "slug", unique = true),
                @Index(name = "idx_tag_name", columnList = "name")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_tag_slug", columnNames = "slug"),
                @UniqueConstraint(name = "uk_tag_name", columnNames = "name")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 70)
    private String slug;                   // SEO-friendly URL slug (e.g., "casual", "waterproof")

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


    // =========== RELATIONSHIPS ===========

    @ManyToMany(mappedBy = "tags")
    @Builder.Default
    private Set<Product> products = new HashSet<>();


    // =========== HELPER METHODS ===========

    public void addProduct(Product product) {
        if (product != null) {
            products.add(product);
            product.getTags().add(this);
        }
    }

    public void removeProduct(Product product) {
        if (product != null) {
            products.remove(product);
            product.getTags().remove(this);
        }
    }
}
