package com.jk.authservice.mapper;

import com.jk.authservice.dto.response.UserAddressResponse;
import com.jk.authservice.dto.response.UserProfileResponse;
import com.jk.authservice.dto.response.AuthResponse;
import com.jk.authservice.dto.response.UserSummaryResponse;
import com.jk.authservice.entity.User;
import com.jk.authservice.entity.UserAddress;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static com.jk.commonlibrary.constants.AppConstants.ACCESS_TOKEN_DURATION_MS;

@Component
public class UserMapper {

    public static UserSummaryResponse mapToUserSummaryResponse(User user){
        return UserSummaryResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toList())
                )
                .accountStatus(user.getAccountStatus())
                .build();
    }

    public static AuthResponse mapToAuthResponse(User user, String accessToken){
        UserSummaryResponse userSummaryResponse = mapToUserSummaryResponse(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .expiresIn(ACCESS_TOKEN_DURATION_MS / 1000)
                .user(userSummaryResponse)
                .build();
    }

    public static UserAddressResponse mapToUserAddressResponse(UserAddress address){
        return UserAddressResponse.builder()
                .id(address.getId())
                .addressType(address.getAddressType())
                .fullName(address.getFullName())
                .phoneNumber(address.getPhoneNumber())
                .streetLine1(address.getStreetLine1())
                .streetLine2(address.getStreetLine2())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .isDefault(address.getIsDefault())
                .build();
    }

    public static List<UserAddressResponse> mapToListOfUserAddressResponses(List<UserAddress> addresses){
        return addresses.stream()
                .map(UserMapper::mapToUserAddressResponse)
                .collect(Collectors.toList());
    }

    public static UserProfileResponse mapToUserProfileResponse(User user){
        List<UserAddressResponse> userAddresses = mapToListOfUserAddressResponses(user.getAddresses());

        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .emailVerified(user.getEmailVerified())
                .accountStatus(user.getAccountStatus())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toList())
                )
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .addresses(userAddresses)
                .build();
    }
}
