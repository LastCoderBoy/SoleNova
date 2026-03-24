package com.jk.authservice.entity;

import com.jk.authservice.enums.AccountStatus;
import com.jk.authservice.enums.RoleName;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_email", columnList = "email"),
                @Index(name = "idx_account_status", columnList = "account_status")
        },
        uniqueConstraints = {
        @UniqueConstraint(name = "uk_email", columnNames = "email")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password; // BCrypt hashed

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false, length = 20)
    private AccountStatus accountStatus = AccountStatus.PENDING_VERIFICATION;

    @Column(name = "account_locked", nullable = false)
    private Boolean accountLocked = false; // the field only checks whether the User is able to login or not

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil; // used when many password attempts are made

    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts = 0;


    // ====================================
    //              METADATA
    // ====================================

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_login_ip",length = 45)
    private String lastLoginIp;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @CreationTimestamp
    @Column(name = "created_at",nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ============= RELATIONSHIPS =============

    // List not Set — order matters (default address should be findable easily)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserAddress> addresses = new ArrayList<>();

    // LAZY - load roles explicitly only when needed (login, token generation)
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();


    // ============= HELPER METHODS =============

    public void addRole(Role role) {
        if (role == null) {
            return;
        }
        this.roles.add(role);

        // Safe bidirectional sync
        if (role.getUsers() != null) {
            role.getUsers().add(this);
        }
    }

    public void removeRole(Role role) {
        if (role == null) {
            return;
        }
        this.roles.remove(role);

        if (role.getUsers() != null) {
            role.getUsers().remove(this);
        }
    }


    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.accountLocked = false;
        this.lockedUntil = null;
    }

    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;

        // Lock account after 5 failed attempts
        if (this.failedLoginAttempts >= 5) {
            this.accountLocked = true;
            this.lockedUntil = LocalDateTime.now().plusMinutes(5); // Lock for 5 minutes
        }
    }


    public String getFullName(){
        return (lastName != null)
                ? firstName + " " + lastName
                : firstName; // firstName is always present
    }

    public boolean hasRole(RoleName roleName) {
        return roles.stream()
                .anyMatch(role -> role.getName() == roleName);
    }
}
