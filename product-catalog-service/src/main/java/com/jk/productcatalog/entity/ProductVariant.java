package com.jk.productcatalog.entity;

import com.jk.productcatalog.exception.InsufficientStockException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ProductVariant entity representing specific SKU with size, color, price, and stock
 * Each product can have multiple variants
 * Example: Nike Air Max - Size US 10 - Red
 */
@Entity
@Table(
        name = "product_variants",
        indexes = {
                @Index(name = "idx_variant_sku", columnList = "sku", unique = true),
                @Index(name = "idx_variant_product", columnList = "product_id"),
                @Index(name = "idx_variant_active_stock", columnList = "isActive, stock_quantity")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_variant_sku", columnNames = "sku"),
                @UniqueConstraint(
                        name = "uk_variant_product_size_color",
                        columnNames = {"product_id", "size", "color"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String sku;                    // Stock Keeping Unit (e.g., "NIKE-AIR-MAX-US10-RED")

    @Column(nullable = false, length = 20)
    private String size;                   // Size (e.g., "US 10", "EU 42", "UK 8")

    @Column(nullable = false, length = 50)
    private String color;                  // Color name (e.g., "Brown", "Black", "White")

    @Column(name = "color_hex", length = 7)
    private String colorHex;             // "#000000" — for color swatch UI


    // ==================== Inventory ====================

    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;

    @Column(name = "reserved_quantity", nullable = false)
    @Builder.Default
    private Integer reservedQuantity = 0; // stock held during checkout

    @Column(name = "low_stock_threshold", nullable = false)
    @Builder.Default
    private Integer lowStockThreshold = 5; // trigger "low stock" warning in UI


    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;         // Availability flag

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    // =========== RELATIONSHIPS ===========

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();


    // =========== HELPER METHODS ===========

    public void addImage(ProductImage image) {
        if (image != null) {
            images.add(image);
            image.setVariant(this);
        }
    }

    public void removeImage(ProductImage image) {
        if (image != null) {
            images.remove(image);
            image.setVariant(null);
        }
    }

    public int getAvailableStock() {
        return Math.max(0, stockQuantity - reservedQuantity);
    }

    public boolean isInStock() {
        return isActive && stockQuantity > 0;
    }

    public boolean isLowStock() {
        return isInStock() && getAvailableStock() <= lowStockThreshold;
    }

    public boolean canReserve(int quantity) {
        return getAvailableStock() >= quantity;
    }

    // Called when order is placed — moves stock to reserved
    public void reserve(int quantity) {
        if (!canReserve(quantity)) {
            throw new InsufficientStockException(
                    "Insufficient stock for SKU: " + sku
                            + ". Available: " + getAvailableStock()
                            + ", requested: " + quantity);
        }
        this.reservedQuantity += quantity;
    }

    // Called when order is confirmed — deducts from actual stock
    public void confirmReservation(int quantity) {
        this.reservedQuantity = Math.max(0, reservedQuantity - quantity);
        this.stockQuantity    = Math.max(0, stockQuantity - quantity);
    }

    // Called when order is cancelled — releases reserved stock
    public void releaseReservation(int quantity) {
        this.reservedQuantity = Math.max(0, reservedQuantity - quantity);
    }

    public void incrementStock(int quantity) {
        if (quantity > 0) {
            this.stockQuantity += quantity;
        }
    }

    public void decrementStock(int quantity) {
        if (quantity > 0 && this.stockQuantity >= quantity) {
            this.stockQuantity -= quantity;
        } else {
            throw new IllegalStateException("Insufficient stock. Available: " + this.stockQuantity + ", Requested: " + quantity);
        }
    }

    public void setStock(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
        this.stockQuantity = quantity;
    }
}
