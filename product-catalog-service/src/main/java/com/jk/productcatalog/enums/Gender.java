package com.jk.productcatalog.enums;

import lombok.Getter;

/**
 * Gender categories for products
 * Used for filtering and categorizing shoes by target audience
 */
@Getter
public enum Gender {
    MALE("Male", "Men's shoes and footwear"),
    FEMALE("Female", "Women's shoes and footwear"),
    UNISEX("Unisex", "Shoes suitable for all genders");

    private final String displayName;
    private final String description;

    Gender(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
