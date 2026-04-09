package com.jk.authservice.controller;

import com.jk.authservice.dto.request.AssignRoleRequest;
import com.jk.authservice.dto.request.UpdateAccountStatusRequest;
import com.jk.authservice.dto.request.UserFilterRequest;
import com.jk.authservice.dto.response.AdminUserResponse;
import com.jk.authservice.entity.UserPrincipal;
import com.jk.authservice.enums.AccountStatus;
import com.jk.authservice.enums.RoleName;
import com.jk.authservice.service.AdminService;
import com.jk.commonlibrary.dto.ApiResponse;
import com.jk.commonlibrary.dto.PaginatedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.jk.commonlibrary.constants.AppConstants.*;

@RestController
@RequestMapping(AUTH_PATH + "/admin") // Only accessible by admins, validated in the Gateway Filter
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin", description = "Admin-only user management endpoints")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    @Operation(summary = "Get all users with pagination and sorting options")
    public ResponseEntity<ApiResponse<PaginatedResponse<AdminUserResponse>>> getAllUsers(
            @ModelAttribute @Valid UserFilterRequest filterRequest) {

        log.info("[ADMIN-CONTROLLER] Get all users - page: {}, size: {}", filterRequest.getPage(), filterRequest.getSize());
        PaginatedResponse<AdminUserResponse> users = adminService.getAllUsers(filterRequest);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Get a specific user by ID")
    public ResponseEntity<ApiResponse<AdminUserResponse>> getUserById(@PathVariable Long userId) {

        log.info("[ADMIN-CONTROLLER] Get user by ID: {}", userId);
        AdminUserResponse user = adminService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
    }

    @PatchMapping("/users/{userId}/status")
    @Operation(summary = "Update user account status")
    public ResponseEntity<ApiResponse<AdminUserResponse>> updateAccountStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateAccountStatusRequest request,
            @AuthenticationPrincipal UserPrincipal adminPrincipal) {

        log.info("[ADMIN-CONTROLLER] Update status for user {} to {} by admin {}",
                userId, request.getAccountStatus(), adminPrincipal.getId());
        AdminUserResponse updated = adminService.updateAccountStatus(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Account status updated successfully", updated));
    }

    @PostMapping("/users/{userId}/roles")
    @Operation(summary = "Assign a role to a user")
    public ResponseEntity<ApiResponse<AdminUserResponse>> assignRole(
            @PathVariable Long userId,
            @Valid @RequestBody AssignRoleRequest request,
            @AuthenticationPrincipal UserPrincipal adminPrincipal) {

        log.info("[ADMIN-CONTROLLER] Assign role {} to user {} by admin {}",
                request.getRole(), userId, adminPrincipal.getId());
        AdminUserResponse updated = adminService.assignRole(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Role assigned successfully", updated));
    }

    @DeleteMapping("/users/{userId}/roles/{roleName}")
    @Operation(summary = "Remove a role from a user")
    public ResponseEntity<ApiResponse<AdminUserResponse>> removeRole(
            @PathVariable Long userId,
            @PathVariable RoleName roleName,
            @AuthenticationPrincipal UserPrincipal adminPrincipal) {

        log.info("[ADMIN-CONTROLLER] Remove role {} from user {} by admin {}",
                roleName, userId, adminPrincipal.getId());
        AdminUserResponse updated = adminService.removeRole(userId, roleName);
        return ResponseEntity.ok(ApiResponse.success("Role removed successfully", updated));
    }

    @PostMapping("/users/{userId}/unlock")
    @Operation(summary = "Unlock a locked user account")
    public ResponseEntity<ApiResponse<AdminUserResponse>> unlockAccount(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal adminPrincipal) {

        log.info("[ADMIN-CONTROLLER] Unlock account for user {} by admin {}", userId, adminPrincipal.getId());
        AdminUserResponse updated = adminService.unlockAccount(userId);
        return ResponseEntity.ok(ApiResponse.success("Account unlocked successfully", updated));
    }

    @DeleteMapping("/users/{userId}/sessions")
    @Operation(summary = "Revoke all sessions for a user (force logout)")
    public ResponseEntity<ApiResponse<Void>> revokeAllSessions(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal adminPrincipal) {

        log.info("[ADMIN-CONTROLLER] Revoke all sessions for user {} by admin {}", userId, adminPrincipal.getId());
        adminService.revokeAllSessions(userId);
        return ResponseEntity.ok(ApiResponse.success("All sessions revoked successfully"));
    }
}
