package com.jk.authservice.service.impl;

import com.jk.authservice.config.AuthCookiesManager;
import com.jk.authservice.config.redis.RedisService;
import com.jk.authservice.config.security.JwtTokenProcessor;
import com.jk.authservice.dto.request.ChangeEmailRequest;
import com.jk.authservice.dto.request.ChangePasswordRequest;
import com.jk.authservice.dto.request.UpdateProfileRequest;
import com.jk.authservice.dto.response.UserProfileResponse;
import com.jk.authservice.entity.EmailToken;
import com.jk.authservice.entity.User;
import com.jk.authservice.enums.TokenType;
import com.jk.authservice.exception.DuplicateResourceFoundException;
import com.jk.authservice.repository.UserRepository;
import com.jk.authservice.service.RefreshTokenService;
import com.jk.authservice.service.UserProfileService;
import com.jk.authservice.service.email.EmailService;
import com.jk.authservice.service.email.EmailTokenService;
import com.jk.commonlibrary.exception.ResourceNotFoundException;
import com.jk.commonlibrary.exception.ValidationException;
import com.jk.commonlibrary.utils.TokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import static com.jk.authservice.mapper.UserMapper.mapToUserProfileResponse;
import static com.jk.commonlibrary.constants.AppConstants.AUTHORIZATION_HEADER;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileServiceImpl implements UserProfileService {

    private final AuthCookiesManager cookiesManager;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProcessor tokenProcessor;
    private final RedisService redisService;
    private final PasswordEncoder passwordEncoder;
    private final EmailTokenService emailTokenService;
    private final EmailService emailService;

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        // Check cache first (Cache-Aside pattern)
        UserProfileResponse cachedUserProfile = redisService.getCachedUserProfile(userId);
        if(cachedUserProfile != null){
            log.debug("[PROFILE-SERVICE] User profile retrieved from Cache for user: {}", userId);
            return cachedUserProfile;
        }

        User user = findUserById(userId);
        UserProfileResponse userProfileResponse = mapToUserProfileResponse(user);

        // Cache for future requests
        redisService.cacheUserProfile(user.getId(), userProfileResponse);

        return userProfileResponse;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserProfileResponse updateProfile(UpdateProfileRequest request, Long id) {
        if(!request.isAtLeastOneFieldProvided()){
            throw new ValidationException("At least one field must be provided for update");
        }

        // find the User and update the fields
        User user = findUserById(id);
        if(request.getFirstName() != null){
            user.setFirstName(request.getFirstName());
        }
        if(request.getLastName() != null){
            user.setLastName(request.getLastName());
        }

        user = userRepository.save(user);
        log.info("[PROFILE-SERVICE] User profile updated successfully for user ID: {}", id);

        // Invalidate and refresh cache after update
        redisService.refreshUserProfile(user.getId(), mapToUserProfileResponse(user));

        return mapToUserProfileResponse(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(ChangePasswordRequest request, Long id, HttpServletResponse httpResponse) {
        User userEntity = findUserById(id);
        String savedPassword = userEntity.getPassword();

        // Validate the current password
        boolean isCurrentPasswordValid = passwordEncoder.matches(request.getCurrentPassword(), savedPassword);
        if(!isCurrentPasswordValid){
            throw new BadCredentialsException("Current password is incorrect");
        }

        // checks the match of the new password and confirm password
        if(!request.isPasswordsMatch()){
            throw new ValidationException("Confirm password does not match with the new password");
        }

        if (passwordEncoder.matches(request.getNewPassword(), savedPassword)) {
            throw new ValidationException("New password must be different from current password");
        }

        userEntity.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userEntity.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(userEntity);

        cookiesManager.clearRefreshTokenCookie(httpResponse);
        log.debug("[PROFILE-SERVICE] Refresh token cookie cleared");

        refreshTokenService.revokeAllRefreshTokensAsync(id);

        log.info("[PROFILE-SERVICE] Password changed successfully for user ID: {})", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void requestEmailChange(ChangeEmailRequest request, Long id){
        User userEntity = findUserById(id);

        if (!passwordEncoder.matches(request.getCurrentPassword(), userEntity.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        if (userRepository.existsByEmail(request.getNewEmail())) {
            throw new DuplicateResourceFoundException("Email already in use");
        }

        // Invalidate old tokens & Generate new ones & Send verification email
        try {
            emailTokenService.revokeUserTokens(id, TokenType.EMAIL_CHANGE_CONFIRMATION);
            EmailToken verificationToken = emailTokenService.createEmailToken(userEntity, TokenType.EMAIL_CHANGE_CONFIRMATION);
            emailService.sendVerificationEmail(
                    userEntity,
                    verificationToken
            );
        } catch (Exception e) {
            log.error("[PROFILE-SERVICE] Failed to send verification email: {}", e.getMessage());
            // No need to break...
        }


    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logout(Long userId, HttpServletResponse response, HttpServletRequest request) {
        try {
            // Revoke Refresh Token
            Optional<String> refreshToken = cookiesManager.extractRefreshTokenFromCookie(request);
            refreshToken.ifPresent(refreshTokenService::revokeRefreshToken);

            // Blacklist the Access Token in Redis
            String authHeader = request.getHeader(AUTHORIZATION_HEADER);
            blacklistAccessToken(authHeader);

            cookiesManager.clearRefreshTokenCookie(response);
            log.info("[PROFILE-SERVICE] Logout successful for user ID: {}", userId);

        } catch (ResourceNotFoundException e) {
            log.warn("[PROFILE-SERVICE] User not found during logout: {}", e.getMessage());

        } catch (Exception e) {
            log.error("[PROFILE-SERVICE] Error during logout for user ID: {}: {}", userId, e.getMessage());

        } finally {
            // ALWAYS clear cookies, regardless of any exception
            cookiesManager.clearRefreshTokenCookie(response);
            log.debug("[PROFILE-SERVICE] Refresh token cookie cleared for user ID: {}", userId);
        }
    }

    /**
     * Logout from all devices asynchronously
     * Runs AFTER response is sent to client
     *
     * @param id User ID
     * @param httpResponse
     */
    @Override
    @Transactional
    public void logoutAll(Long id, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        try{
            // Revoke Refresh Token
            refreshTokenService.revokeAllRefreshTokensAsync(id);

            // Blacklist the Access Token in Redis
            String authHeader = httpRequest.getHeader(AUTHORIZATION_HEADER);
            blacklistAccessToken(authHeader);

            log.info("[PROFILE-SERVICE] Logout all successful for user ID: {}", id);

        } catch (Exception e){
            // Don't throw - this is async, exceptions are logged by AsyncUncaughtExceptionHandler
            log.error("[PROFILE-SERVICE] Error during async logout for user {}: {}",
                    id, e.getMessage(), e);
        } finally {
            cookiesManager.clearRefreshTokenCookie(httpResponse);
            log.debug("[PROFILE-SERVICE] Refresh token cookie cleared for user ID: {} on logoutAll() request.", id);
        }
    }

    // ==========================================
    //               HELPER METHODS
    // ==========================================

    @Transactional(readOnly = true)
    public User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow( () -> {
                    log.error("[PROFILE-SERVICE] User not found with ID: {}", id);
                    return new ResourceNotFoundException("User not found");
                });
    }


    private void blacklistAccessToken(String authHeader){
        String accessToken = TokenUtils.validateAndExtractToken(authHeader); // might throw InvalidTokenException
        Date tokenExpiration = tokenProcessor.getExpirationDateFromToken(accessToken);
        long remainingTtl = tokenExpiration.getTime() - System.currentTimeMillis();

        if (remainingTtl > 0) {
            redisService.blackListToken(accessToken, remainingTtl);
            log.debug("[PROFILE-SERVICE] Access token blacklisted for {}ms", remainingTtl);
        } else {
            log.debug("[PROFILE-SERVICE] Access token already expired, skipping blacklist");
        }
    }
}
