package com.jk.authservice.enums;

import lombok.Getter;

@Getter
public enum RoleName {
    ROLE_USER("Standard customer user"),
    ROLE_ADMIN("Full system access, including user and configuration management"),
    ROLE_MANAGER("Managerial access");

    private final String description;

    RoleName(String description) {
        this.description = description;
    }
}

