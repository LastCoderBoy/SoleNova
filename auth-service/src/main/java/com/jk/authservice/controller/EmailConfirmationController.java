package com.jk.authservice.controller;

import com.jk.authservice.dto.request.ResendVerificationRequest;
import com.jk.authservice.entity.UserPrincipal;
import com.jk.authservice.service.email.EmailService;
import com.jk.authservice.service.email.impl.EmailServiceImpl;
import com.jk.commonlibrary.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.jk.commonlibrary.constants.AppConstants.AUTH_PATH;

@RestController
@RequestMapping(AUTH_PATH)
@RequiredArgsConstructor
@Slf4j
public class EmailConfirmationController {

    private final EmailService emailService;

    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        log.info("[EMAIL-CONTROLLER] Email verification request received");

        emailService.verifyEmail(token);

        return ResponseEntity.ok(
                ApiResponse.success("Email verified successfully! You can now log in.")
        );
    }

    // Confirm email change using token sent to new address
    @PostMapping("/confirm-email-change")
    public ResponseEntity<ApiResponse<Void>> confirmEmailChange(String token,
                                                                @AuthenticationPrincipal UserPrincipal principal) {

        log.info("[PROFILE-CONTROLLER] Confirm email change for user ID: {}", principal.getId());
        emailService.confirmEmailChange(token, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Email updated successfully."));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @Valid @RequestBody ResendVerificationRequest resendVerificationRequest) {

        log.info("[EMAIL-CONTROLLER] Resend verification request for user: {}",
                resendVerificationRequest.getEmail());

        emailService.resendVerificationEmail(resendVerificationRequest.getEmail());

        return ResponseEntity.ok(
                ApiResponse.success("Verification email sent. Please check your inbox.")
        );
    }

}
