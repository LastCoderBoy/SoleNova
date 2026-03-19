package com.jk.authservice.controller;


import com.jk.authservice.controller.docs.*;
import com.jk.authservice.dto.*;
import com.jk.authservice.dto.request.*;
import com.jk.authservice.dto.response.AuthResponse;
import com.jk.authservice.dto.response.UserAddressResponse;
import com.jk.authservice.dto.response.UserProfileResponse;
import com.jk.authservice.entity.UserPrincipal;
import com.jk.authservice.service.AuthenticationService;
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

import static com.jk.commonlibrary.constants.AppConstants.AUTH_PATH;

/**
 * Authentication Controller
 * Handles user registration, login, logout, profile management, and JWT token refresh
 * @author LastCoderBoy
 */
@RestController
@RequestMapping(AUTH_PATH)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "User authentication and authorization endpoints")
public class AuthenticationController {

    private final AuthenticationService authService;

    @RegisterDocs
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest registerRequest,
                                                              HttpServletResponse response,
                                                              HttpServletRequest request) {
        // Base validation is done via @Valid annotation
        // processing the request to the service layer
        log.info("[AUTH-CONTROLLER] Register request for email: {}", registerRequest.getEmail());
        AuthResponse authResponse = authService.register(registerRequest, response, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful. Please verify your email.", authResponse));
    }

    @LoginDocs
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest loginRequest,
                                                           HttpServletRequest request,
                                                           HttpServletResponse response) {
        log.info("[AUTH-CONTROLLER] Logging the user with email: {}", loginRequest.getEmail());

        AuthResponse authResponse = authService.login(loginRequest, request, response);
        return ResponseEntity.ok(ApiResponse.success("User logged in successfully", authResponse));
    }

    // refresh token will be extracted from the cookies
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshJwtToken(HttpServletRequest request,
                                                                     HttpServletResponse response) {
        log.info("[AUTH-CONTROLLER] Refreshing JWT tokens...");
        AuthResponse authResponse = authService.refreshJwtTokens(request, response);

        return ResponseEntity.ok(
                ApiResponse.success("JWT tokens refreshed successfully", authResponse)
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {

        log.info("[AUTH-CONTROLLER] Forgot password request for email: {}", request.getEmail());
        authService.forgotPassword(request.getEmail());

        // Do Not reveal whether email exists (prevents user enumeration)
        return ResponseEntity.ok(ApiResponse.success(
                "If that email is registered, a password reset link has been sent."
        ));
    }
}
