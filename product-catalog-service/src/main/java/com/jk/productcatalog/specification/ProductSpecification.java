package com.jk.productcatalog.specification;


import com.jk.productcatalog.entity.Category;
import com.jk.productcatalog.entity.Product;
import com.jk.productcatalog.entity.ProductVariant;
import com.jk.productcatalog.entity.Tag;
import com.jk.productcatalog.enums.Gender;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class ProductSpecification {

    public static Specification<Product> hasSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) return null;
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("slug")), pattern),
                    cb.like(cb.lower(root.get("gender")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    /**
     * Filter by category ID
     */
    public static Specification<Product> hasCategoryId(Long categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) return cb.conjunction();
            return cb.equal(root.get("category").get("id"), categoryId);
        };
    }

    /**
     * Filter by brand ID
     */
    public static Specification<Product> hasBrandId(Long brandId) {
        return (root, query, cb) -> {
            if (brandId == null) return cb.conjunction();
            return cb.equal(root.get("brand").get("id"), brandId);
        };
    }

    /**
     * Filter by gender
     */
    public static Specification<Product> hasGender(Gender gender) {
        return (root, query, cb) -> {
            if (gender == null) return cb.conjunction();
            return cb.equal(root.get("gender"), gender);
        };
    }

    /**
     * Filter by featured products
     */
    public static Specification<Product> isFeatured(Boolean featured) {
        return (root, query, cb) -> {
            if (featured == null) return cb.conjunction();
            return cb.equal(root.get("isFeatured"), featured);
        };
    }

    /**
     * Filter by active products only
     */
    public static Specification<Product> isActive(Boolean active) {
        return (root, query, cb) -> {
            if (active == null) return cb.conjunction();
            return cb.equal(root.get("isActive"), active);
        };
    }

    /**
     * Filter by colors (products that have at least one variant with matching color)
     */
    public static Specification<Product> hasColors(List<String> colors) {
        return (root, query, cb) -> {
            if (colors == null || colors.isEmpty()) return cb.conjunction();

            query.distinct(true);

            Join<Product, ProductVariant> variantJoin = root.join("variants");
            return variantJoin.get("color").in(colors);
        };
    }

    /**
     * Filter by tags
     */
    public static Specification<Product> hasTags(List<String> tagSlugs) {
        return (root, query, cb) -> {
            if (tagSlugs == null || tagSlugs.isEmpty()) return cb.conjunction();

            query.distinct(true);

            Join<Product, Tag> tagJoin = root.join("tags");
            return tagJoin.get("slug").in(tagSlugs);
        };
    }
}
