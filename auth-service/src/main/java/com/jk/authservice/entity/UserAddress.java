package com.jk.authservice.entity;

import com.jk.authservice.enums.AddressType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_addresses",
        indexes = {
                @Index(name = "idx_address_user_id", columnList = "user_id"),
                @Index(name = "idx_address_default", columnList = "user_id, is_default")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", nullable = false, length = 20)
    private AddressType addressType;   // SHIPPING, BILLING

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;           // Recipient name (may differ from account name)

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "street_line1", nullable = false, length = 255)
    private String streetLine1;

    @Column(name = "street_line2", length = 255)
    private String streetLine2;        // Apt, Suite, Floor etc.

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String state;              // State / Province / Region

    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    @Column(nullable = false, length = 2)
    private String country;            // ISO 3166-1 alpha-2 (e.g. "US", "GB", "DE")

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    // =========== RELATIONSHIPS ===========

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}
