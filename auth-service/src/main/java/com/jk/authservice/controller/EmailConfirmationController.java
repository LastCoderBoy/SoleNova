package com.jk.authservice.controller;

import com.jk.authservice.dto.request.ResetPasswordRequest;
import com.jk.authservice.entity.UserPrincipal;
import com.jk.authservice.service.email.EmailService;
import com.jk.commonlibrary.dto.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
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
    public ResponseEntity<ApiResponse<Void>> clickVerificationEmailLink(@RequestParam String token) {
        log.info("[EMAIL-CONTROLLER] Email verification request received");

        emailService.clickVerificationEmailLink(token);

        return ResponseEntity.ok(
                ApiResponse.success("Email verified successfully! You can now log in.")
        );
    }

//    // Confirm email change using token sent to new address
//    @PostMapping("/confirm-email-change")
//    public ResponseEntity<ApiResponse<Void>> confirmEmailChange(@RequestParam String token,
//                                                                @AuthenticationPrincipal UserPrincipal principal) {
//
//        log.info("[PROFILE-CONTROLLER] Confirm email change for user ID: {}", principal.getId());
//        emailService.confirmEmailChange(token, principal.getId());
//        return ResponseEntity.ok(ApiResponse.success("Email updated successfully."));
//    }
}
