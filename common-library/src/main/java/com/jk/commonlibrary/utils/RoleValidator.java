package com.jk.commonlibrary.utils;

import com.jk.commonlibrary.exception.ForbiddenException;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class RoleValidator {

    // Safe version
    private Set<String> parseRoles(String userRoles) {
        return Arrays.stream(userRoles.split(","))
                .map(String::trim)
                .filter(r -> !r.isEmpty())
                .collect(Collectors.toSet());
    }

    public void requireAnyRole(String userRoles, String... requiredRoles) {
        Set<String> userRoleSet = parseRoles(userRoles);
        Set<String> required = new HashSet<>(Arrays.asList(requiredRoles));

        boolean hasRole = required.stream().anyMatch(userRoleSet::contains);
        if (!hasRole) {
            log.warn("[ROLE-VALIDATOR] Access denied - Required: {}, User has: {}",
                    required, userRoleSet);
            throw new ForbiddenException("Insufficient permissions");
        }
    }

    /**
     * Check if user has any of the specified roles (no exception)
     */
    public boolean hasAnyRole(String userRoles, String... requiredRoles) {
        if (userRoles == null || userRoles.trim().isEmpty()) {
            return false;
        }
        return Arrays.stream(requiredRoles)
                .anyMatch(userRoles::contains);
    }
}
