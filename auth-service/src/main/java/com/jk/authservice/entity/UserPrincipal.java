package com.jk.authservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jk.authservice.enums.AccountStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User Principal - Spring Security UserDetails implementation
 * Wraps Users entity for authentication
 *
 * @author LastCoderBoy
 */
@Getter
@Builder
@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {

    // ==================== Core Identity Fields ====================
    private final Long id;
    private final String email;

    // ==================== Security Fields ====================
    @JsonIgnore
    private final String password;  // Only used during authentication

    @JsonIgnore
    private final AccountStatus accountStatus;

    @JsonIgnore
    private final Boolean accountLocked;

    @JsonIgnore
    private final LocalDateTime lockedUntil;

    @JsonIgnore
    private final Boolean emailVerified;

    @JsonIgnore
    private final Collection<? extends GrantedAuthority> authorities;

    // ==================== Factory Methods ====================

    /**
     * Create UserPrincipal from User entity (for login)
     * Used by CustomUserDetailsService
     */
    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());

        return UserPrincipal.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .accountStatus(user.getAccountStatus())
                .accountLocked(user.getAccountLocked())
                .lockedUntil(user.getLockedUntil())
                .emailVerified(user.getEmailVerified())
                .authorities(authorities)
                .build();
    }

    /**
     * Create UserPrincipal from JWT claims (for JWT-based authentication)
     * Used by JwtFilter
     */
    public static UserPrincipal fromJwtClaims(Long userId, String email, List<String> roles) {
        List<GrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return UserPrincipal.builder()
                .id(userId)
                .email(email)
                .password(null)  // Not in JWT
                .accountStatus(AccountStatus.ACTIVE)  // Assume active if JWT is valid
                .emailVerified(true)  // Assume verified if JWT is valid
                .authorities(authorities)
                .build();
    }

    // ==================== UserDetails Implementation ====================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;  // We use AccountStatus instead
    }

    /**
     * Check if account is locked
     * CRITICAL: This is called by Spring Security during authentication!
     */
    @Override
    public boolean isAccountNonLocked() {
        if (accountLocked == null || !accountLocked) {
            return true;  // Not locked
        }

        // Check if lock has expired
        if (lockedUntil != null && LocalDateTime.now().isAfter(lockedUntil)) {
            return true;  // Lock expired, allow login
        }

        return false;  // Still locked
    }

    @Override
    public boolean isEnabled() {
        return accountStatus == AccountStatus.ACTIVE && Boolean.TRUE.equals(emailVerified);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // We handle password expiry separately if needed
    }

    // ==================== Helper Methods ====================
    public boolean hasRole(String roleName) {
        String roleToCheck = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals(roleToCheck));
    }

    public boolean hasAnyRole(String... roleNames) {
        return Arrays.stream(roleNames)
                .anyMatch(this::hasRole);
    }

    public List<String> getListOfRoles() {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }
}
