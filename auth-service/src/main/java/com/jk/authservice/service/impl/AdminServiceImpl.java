package com.jk.authservice.service.impl;

import com.jk.authservice.config.redis.RedisService;
import com.jk.authservice.dto.request.AssignRoleRequest;
import com.jk.authservice.dto.request.UpdateAccountStatusRequest;
import com.jk.authservice.dto.response.AdminUserResponse;
import com.jk.authservice.entity.Role;
import com.jk.authservice.entity.User;
import com.jk.authservice.enums.AccountStatus;
import com.jk.authservice.enums.RoleName;
import com.jk.authservice.mapper.UserMapper;
import com.jk.authservice.queryService.RoleQueryService;
import com.jk.authservice.repository.UserRepository;
import com.jk.authservice.service.AdminService;
import com.jk.authservice.service.RefreshTokenService;
import com.jk.commonlibrary.exception.ResourceNotFoundException;
import com.jk.commonlibrary.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final RoleQueryService roleQueryService;
    private final RedisService redisService;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserResponse> getAllUsers(int page, int size, String sortBy, String sortDir,
                                               AccountStatus status, String search) {
        log.info("[ADMIN-SERVICE] Get all users - page: {}, size: {}, status: {}, search: {}",
                page, size, status, search);

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> users;
        if (search != null && !search.isBlank()) {
            if (status != null) {
                users = userRepository.searchUsers(search, status, pageable);
            } else {
                users = userRepository.searchUsers(search, null, pageable);
            }
        } else if (status != null) {
            users = userRepository.findByAccountStatus(status, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        return users.map(UserMapper::mapToAdminUserResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserResponse getUserById(Long userId) {
        log.info("[ADMIN-SERVICE] Get user by ID: {}", userId);

        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        return UserMapper.mapToAdminUserResponse(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminUserResponse updateAccountStatus(Long userId, UpdateAccountStatusRequest request) {
        log.info("[ADMIN-SERVICE] Update account status for user {} to {}", userId, request.getAccountStatus());

        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        AccountStatus newStatus = request.getAccountStatus();
        AccountStatus currentStatus = user.getAccountStatus();

        if (currentStatus == newStatus) {
            throw new ValidationException("User is already in status: " + newStatus);
        }

        user.setAccountStatus(newStatus);

        if (newStatus == AccountStatus.SUSPENDED || newStatus == AccountStatus.CLOSED) {
            refreshTokenService.revokeAllRefreshTokensAsync(userId);
            redisService.invalidateUserProfile(userId);
        }

        if (newStatus == AccountStatus.ACTIVE && user.getAccountLocked()) {
            user.resetFailedLoginAttempts();
        }

        User savedUser = userRepository.save(user);
        log.info("[ADMIN-SERVICE] User {} status updated from {} to {}",
                userId, currentStatus, newStatus);

        return UserMapper.mapToAdminUserResponse(savedUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminUserResponse assignRole(Long userId, AssignRoleRequest request) {
        log.info("[ADMIN-SERVICE] Assign role {} to user {}", request.getRole(), userId);

        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Role role = roleQueryService.findByName(request.getRole());

        if (user.hasRole(request.getRole())) {
            throw new ValidationException("User already has role: " + request.getRole());
        }

        user.addRole(role);
        User savedUser = userRepository.save(user);

        redisService.invalidateUserProfile(userId);

        log.info("[ADMIN-SERVICE] Role {} assigned to user {}", request.getRole(), userId);
        return UserMapper.mapToAdminUserResponse(savedUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminUserResponse removeRole(Long userId, RoleName roleName) {
        log.info("[ADMIN-SERVICE] Remove role {} from user {}", roleName, userId);

        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (roleName == RoleName.ROLE_USER) {
            throw new ValidationException("Cannot remove ROLE_USER from a user");
        }

        if (!user.hasRole(roleName)) {
            throw new ValidationException("User does not have role: " + roleName);
        }

        Role role = roleQueryService.findByName(roleName);
        user.removeRole(role);

        User savedUser = userRepository.save(user);

        redisService.invalidateUserProfile(userId);

        log.info("[ADMIN-SERVICE] Role {} removed from user {}", roleName, userId);
        return UserMapper.mapToAdminUserResponse(savedUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminUserResponse unlockAccount(Long userId) {
        log.info("[ADMIN-SERVICE] Unlock account for user {}", userId);

        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (!user.getAccountLocked()) {
            throw new ValidationException("User account is not locked");
        }

        user.resetFailedLoginAttempts();
        user.setAccountStatus(AccountStatus.ACTIVE);

        User savedUser = userRepository.save(user);

        log.info("[ADMIN-SERVICE] User {} account unlocked successfully", userId);
        return UserMapper.mapToAdminUserResponse(savedUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeAllSessions(Long userId) {
        log.info("[ADMIN-SERVICE] Revoke all sessions for user {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }

        refreshTokenService.revokeAllRefreshTokensAsync(userId);
        redisService.invalidateUserProfile(userId);

        log.info("[ADMIN-SERVICE] All sessions revoked for user {}", userId);
    }
}
