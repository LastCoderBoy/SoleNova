package com.jk.authservice.controller;

import com.jk.authservice.controller.docs.GetProfileDocs;
import com.jk.authservice.controller.docs.LogoutDocs;
import com.jk.authservice.controller.docs.UpdateProfileDocs;
import com.jk.authservice.dto.request.*;
import com.jk.authservice.dto.response.UserAddressResponse;
import com.jk.authservice.dto.response.UserProfileResponse;
import com.jk.authservice.entity.UserPrincipal;
import com.jk.authservice.service.UserAddressService;
import com.jk.authservice.service.UserProfileService;
import com.jk.commonlibrary.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.jk.commonlibrary.constants.AppConstants.AUTH_PATH;

@RestController
@RequestMapping(AUTH_PATH + "/user-profile/me")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Profile", description = "Profile management, address management and account settings")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final UserAddressService userAddressService;

    // ===================================================
    //                  PROFILE ENDPOINTS
    // ===================================================

    @GetMapping
    @GetProfileDocs
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(@AuthenticationPrincipal UserPrincipal principal) {

        log.info("[PROFILE-CONTROLLER] Get profile for user ID: {}", principal.getId());

        UserProfileResponse profile = userProfileService.getProfile(principal.getId());

        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", profile));
    }

    @PatchMapping
    @UpdateProfileDocs
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(@Valid @RequestBody UpdateProfileRequest request,
                                                                          @AuthenticationPrincipal UserPrincipal principal) {

        log.info("[PROFILE-CONTROLLER] Update profile for user ID: {}", principal.getId());
        UserProfileResponse updated = userProfileService.updateProfile(request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updated));
    }

    // Invalidates all refresh tokens and clears cookies — user must re-login
    @PatchMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                            @AuthenticationPrincipal UserPrincipal principal,
                                                            HttpServletResponse httpResponse) {

        log.info("[PROFILE-CONTROLLER] Change password for user ID: {}", principal.getId());

        userProfileService.changePassword(request, principal.getId(), httpResponse);
        return ResponseEntity.ok(ApiResponse.success(
                "Password changed successfully. Please log in again."
        ));
    }

    @PatchMapping("/change-email")
    public ResponseEntity<ApiResponse<Void>> changeEmail(@Valid @RequestBody ChangeEmailRequest request,
                                                         @AuthenticationPrincipal UserPrincipal principal) {

        log.info("[PROFILE-CONTROLLER] Change email request for user ID: {}", principal.getId());
        userProfileService.requestEmailChange(request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(
                "A confirmation link has been sent to your new email address."
        ));
    }

    @LogoutDocs
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal UserPrincipal principal,
                                                    HttpServletResponse response, // used for clearing the cookies
                                                    HttpServletRequest request) {
        log.info("[AUTH-CONTROLLER] Logging out user with ID: {}", principal.getId());

        userProfileService.logout(principal.getId(), response, request);
        return ResponseEntity.ok(ApiResponse.success("User logged out successfully"));
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Logout from all devices")
    public ResponseEntity<ApiResponse<Void>> logoutAll(@AuthenticationPrincipal UserPrincipal principal,
                                                       HttpServletRequest httpRequest,
                                                       HttpServletResponse httpResponse) {

        log.info("[AUTH-CONTROLLER] Logout-all request for user ID: {}", principal.getId());
        userProfileService.logoutAll(principal.getId(), httpRequest, httpResponse);
        return ResponseEntity.ok(ApiResponse.success("Logged out from all devices successfully"));
    }

    // ===================================================
    //                  ADDRESS ENDPOINTS
    // ===================================================

    @GetMapping("/addresses")
    public ResponseEntity<ApiResponse<List<UserAddressResponse>>> getAddresses(@AuthenticationPrincipal UserPrincipal principal) {

        log.info("[PROFILE-CONTROLLER] Get addresses for user ID: {}", principal.getId());
        List<UserAddressResponse> addresses = userAddressService.getAddresses(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Addresses retrieved successfully", addresses));
    }

    @PostMapping("/addresses")
    public ResponseEntity<ApiResponse<UserAddressResponse>> createAddress(@Valid @RequestBody CreateAddressRequest request,
                                                                          @AuthenticationPrincipal UserPrincipal principal) {

        log.info("[PROFILE-CONTROLLER] Create address for user ID: {}", principal.getId());
        UserAddressResponse created = userAddressService.createAddress(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Address created successfully", created));
    }

    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<ApiResponse<UserAddressResponse>> updateAddress(@PathVariable Long addressId,
                                                                          @Valid @RequestBody UpdateAddressRequest request,
                                                                          @AuthenticationPrincipal UserPrincipal principal) {

        log.info("[PROFILE-CONTROLLER] Update address {} for user ID: {}", addressId, principal.getId());
        UserAddressResponse updated = userAddressService.updateAddress(request, addressId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Address updated successfully", updated));
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(@PathVariable Long addressId,
                                                           @AuthenticationPrincipal UserPrincipal principal) {

        log.info("[PROFILE-CONTROLLER] Delete address {} for user ID: {}", addressId, principal.getId());
        userAddressService.deleteAddress(addressId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Address deleted successfully"));
    }

    @PatchMapping("/addresses/{addressId}/set-default")
    public ResponseEntity<ApiResponse<UserAddressResponse>> setDefaultAddress(@PathVariable Long addressId,
                                                                              @AuthenticationPrincipal UserPrincipal principal) {

        log.info("[PROFILE-CONTROLLER] Set default address {} for user ID: {}", addressId, principal.getId());
        UserAddressResponse updated = userAddressService.setDefaultAddress(addressId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Default address updated successfully", updated));
    }
}