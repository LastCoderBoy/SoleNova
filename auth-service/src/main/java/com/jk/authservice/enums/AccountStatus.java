package com.jk.authservice.enums;

import lombok.Getter;

@Getter
public enum AccountStatus {
    ACTIVE ("Normal isActive account"),
    INACTIVE ("Temporarily inactive"),
    SUSPENDED ("Suspended due to suspicious activity"),
    CLOSED("Permanently closed"),
    PENDING_VERIFICATION ("Awaiting email verification");

    private final String description;

    AccountStatus(String description){
        this.description = description;
    }
}
