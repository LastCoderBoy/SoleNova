package com.jk.authservice.service;

import com.jk.authservice.dto.request.AssignRoleRequest;
import com.jk.authservice.dto.request.UpdateAccountStatusRequest;
import com.jk.authservice.dto.response.AdminUserResponse;
import com.jk.authservice.enums.AccountStatus;
import com.jk.authservice.enums.RoleName;
import org.springframework.data.domain.Page;

public interface AdminService {

    Page<AdminUserResponse> getAllUsers(int page, int size, String sortBy, String sortDir,
                                       AccountStatus status, String search);

    AdminUserResponse getUserById(Long userId);

    AdminUserResponse updateAccountStatus(Long userId, UpdateAccountStatusRequest request);

    AdminUserResponse assignRole(Long userId, AssignRoleRequest request);

    AdminUserResponse removeRole(Long userId, RoleName roleName);

    AdminUserResponse unlockAccount(Long userId);

    void revokeAllSessions(Long userId);
}
