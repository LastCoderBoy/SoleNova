package com.jk.authservice.dto.response;

import com.jk.authservice.enums.AccountStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfileResponse {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private Boolean emailVerified;
    private AccountStatus accountStatus;
    private List<String> roles;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private List<UserAddressResponse> addresses;
}
