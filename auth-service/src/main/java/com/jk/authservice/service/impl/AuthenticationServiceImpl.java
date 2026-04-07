package com.jk.authservice.service.impl;

import com.jk.authservice.config.AuthCookiesManager;
import com.jk.authservice.config.redis.RedisService;
import com.jk.authservice.config.security.JwtTokenProcessor;
import com.jk.authservice.dto.request.LoginRequest;
import com.jk.authservice.dto.request.RegisterRequest;
import com.jk.authservice.dto.request.ResetPasswordRequest;
import com.jk.authservice.dto.response.AuthResponse;
import com.jk.authservice.dto.response.UserAddressResponse;
import com.jk.authservice.entity.*;
import com.jk.authservice.enums.AccountStatus;
import com.jk.authservice.enums.TokenType;
import com.jk.authservice.exception.AccountLockedException;
import com.jk.authservice.exception.AccountNotVerifiedException;
import com.jk.authservice.exception.DuplicateResourceFoundException;
import com.jk.authservice.mapper.UserMapper;
import com.jk.authservice.queryService.RoleQueryService;
import com.jk.authservice.repository.UserRepository;
import com.jk.authservice.service.AuthenticationService;
import com.jk.authservice.service.RefreshTokenService;
import com.jk.authservice.service.UserAddressService;
import com.jk.authservice.service.email.EmailService;
import com.jk.authservice.service.email.EmailTokenService;
import com.jk.authservice.utils.HeaderExtractor;
import com.jk.commonlibrary.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final EmailTokenService emailTokenService;
    private final EmailService emailService;
    private final AuthCookiesManager cookiesManager;
    private final JwtTokenProcessor tokenProcessor;
    private final RedisService redisService;
    private final RoleQueryService roleQueryService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final UserAddressService userAddressService;

    // =========== REPOSITORIES ===========
    private final UserRepository userRepository;

    @Transactional(rollbackFor = Exception.class)  // Rollback on ANY exception
    @Override
    public AuthResponse register(RegisterRequest registerRequest, HttpServletResponse response, HttpServletRequest request) {
        log.info("[AUTH-SERVICE] Starting registration for user: {}", registerRequest.getEmail());

        if(!registerRequest.isPasswordsMatch()){
            throw new ValidationException("Passwords do not match");
        }

        if(userRepository.existsByEmail(registerRequest.getEmail())){
            throw new DuplicateResourceFoundException("Email already exists");
        }

        // Create and Save User
        User newUser = createUserEntity(registerRequest);
        newUser = userRepository.save(newUser);
        log.info("[AUTH-SERVICE] User saved with ID: {}", newUser.getId());

        // Generate Token & Send verification email
        try {
            EmailToken verificationToken = emailTokenService.createEmailToken(newUser, TokenType.EMAIL_VERIFICATION);
            emailService.sendVerificationEmail(
                    newUser,
                    verificationToken
            );
        } catch (Exception e) {
            log.error("[AUTH-SERVICE] Failed to send verification email: {}", e.getMessage());
            // No need to break...
        }

        // Extract Headers
        String clientIP = HeaderExtractor.extractClientIp(request);
        String userAgent = HeaderExtractor.extractUserAgent(request);

        // Generate Access Token
        List<String> roleNames = newUser.getRoles().stream()
                .map(role -> role.getName().name())
                .toList();

        String accessToken = tokenProcessor.generateAccessToken(
                newUser.getId(),
                newUser.getEmail(),
                roleNames
        );

        // Create and Save Refresh Token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                newUser,
                clientIP,
                userAgent
        );

        // Set the Refresh token cookie
        cookiesManager.setRefreshTokenCookie(response, refreshToken.getToken());

        return UserMapper.mapToAuthResponse(newUser, roleNames, accessToken);
    }

    // No need Transactional
    @Override
    public AuthResponse login(LoginRequest loginRequest, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        log.info("[AUTH-SERVICE] Login attempt for: {}", loginRequest.getEmail());

        try {
            // Spring Security checks in the backend
            // CustomUserDetailsService handles lock reset,
            // UserPrincipal.isEnabled() / isAccountNonLocked() enforce status
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail().toLowerCase(),
                            loginRequest.getPassword()
                    )
            );

            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            log.info("[AUTH-SERVICE] Authentication successful for user: {} (ID: {})",
                    principal.getUsername(), principal.getId());

            // Fetch full user entity with addresses
            User user = findUserById(principal.getId());
            List<UserAddressResponse> userAddresses =
                    userAddressService.getAddresses(principal.getId());// Force load addresses for caching

            String clientIp = HeaderExtractor.extractClientIp(httpRequest);
            String userAgent = HeaderExtractor.extractUserAgent(httpRequest);

            user.setLastLoginAt(LocalDateTime.now());
            user.setLastLoginIp(clientIp);
            user.resetFailedLoginAttempts();
            userRepository.save(user);

            // Generate Access Token
            String accessToken = tokenProcessor.generateAccessToken(
                    principal.getId(),
                    principal.getEmail(),
                    principal.getListOfRoles()
            );

            // Generate Refresh Token
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                    user, clientIp, userAgent
            );

            cookiesManager.setRefreshTokenCookie(httpResponse, refreshToken.getToken());

            // Cache user profile after successful login
            redisService.cacheUserProfile(
                    user.getId(),
                    UserMapper.mapToUserProfileResponse(user, userAddresses, principal.getListOfRoles())
            );

            return UserMapper.mapToAuthResponse(user, principal.getListOfRoles(), accessToken);

        } catch (DisabledException e) {
            // isEnabled() returned false — email not verified OR status not ACTIVE
            log.warn("[AUTH-SERVICE] Disabled account login attempt: {}", loginRequest.getEmail());
            throw new AccountNotVerifiedException(
                    "Account is not isActive. Please verify your email first.");

        } catch (LockedException e) {
            // isAccountNonLocked() returned false
            log.warn("[AUTH-SERVICE] Locked account login attempt: {}", loginRequest.getEmail());
            // Re-fetch to get remaining lock time for the message
            userRepository.findByEmail(loginRequest.getEmail()).ifPresent(u -> {
                if (u.getLockedUntil() != null) {
                    long minutes = Duration.between(
                            LocalDateTime.now(), u.getLockedUntil()).toMinutes();
                    throw new AccountLockedException(
                            "Account locked. Try again in " + minutes + " minutes.");
                }
            });
            throw new AccountLockedException("Account is locked. Please try again later.");
        } catch (BadCredentialsException e) {
            // Wrong password — increment attempts, potentially lock
            log.warn("[AUTH-SERVICE] Bad credentials for: {}", loginRequest.getEmail());
            updateFailedLoginAttempts(loginRequest.getEmail());
            throw new UnauthorizedException("Invalid email or password");
        } catch (AccountLockedException | AccountNotVerifiedException | UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            log.error("[AUTH-SERVICE] Unexpected error during login: {}", e.getMessage(), e);
            throw new InternalServerException("Unexpected error occurred!");
        }
    }

    @Transactional
    @Override
    public AuthResponse refreshJwtTokens(HttpServletRequest request, HttpServletResponse response) {
        String rawRefreshToken = cookiesManager.extractRefreshTokenFromCookie(request)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found. Please log in again."));

        // Extract Headers
        String clientIp = HeaderExtractor.extractClientIp(request);
        String userAgent = HeaderExtractor.extractUserAgent(request);

        // verify the old refresh token and create a new one
        RefreshToken storedToken = refreshTokenService.verifyRefreshToken(
                rawRefreshToken,
                response); // method will resolve the Lazy Exception

        User user = storedToken.getUser();
        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new UnauthorizedException("Account is no longer isActive.");
        }

        // Generate new Refresh Token
        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(
                storedToken,
                clientIp,
                userAgent
        );

        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(clientIp);
        userRepository.save(user);

        // Generate new Access Token
        Long userId = user.getId();
        String email = user.getEmail();
        List<String> userRoles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .toList();

        String accessToken = tokenProcessor.generateAccessToken(userId, email, userRoles);

        // Set the Refresh token cookie
        cookiesManager.setRefreshTokenCookie(response, newRefreshToken.getToken());

        return UserMapper.mapToAuthResponse(user, userRoles, accessToken);
    }

    @Transactional
    @Override
    public void forgotPassword(String email) {
        log.info("[AUTH-SERVICE] Forgot password requested for: {}", email);

        userRepository.findByEmail(email).ifPresent(user -> {

            // Revoke any existing unused reset tokens for this user
            emailTokenService.revokeUserTokens(user.getId(), TokenType.PASSWORD_RESET);

            EmailToken emailToken = emailTokenService.createEmailToken(user, TokenType.PASSWORD_RESET);

            // Send async — user sees response immediately
            emailService.sendForgotPasswordEmail(user, emailToken);

        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(String emailToken, ResetPasswordRequest request, HttpServletResponse httpResponse){
        log.info("[AUTH-SERVICE] Processing password reset");

        if(!request.isPasswordsMatch()){
            throw new ValidationException("Passwords do not match");
        }

        //  marks it as used inside the method
        User userEntity = emailTokenService.verifyToken(emailToken, TokenType.PASSWORD_RESET);

        // 3. Prevent reuse of same password
        if (passwordEncoder.matches(request.getNewPassword(), userEntity.getPassword())) {
            throw new ValidationException(
                    "New password must be different from your current password");
        }

        userEntity.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userEntity.setPasswordChangedAt(LocalDateTime.now());
        userEntity.resetFailedLoginAttempts();
        userRepository.save(userEntity);

        // Clear cookie and revoke all refresh tokens — force re-login everywhere
        cookiesManager.clearRefreshTokenCookie(httpResponse);
        refreshTokenService.revokeAllRefreshTokensAsync(userEntity.getId());

        // Invalidate cached profile
        redisService.invalidateUserProfile(userEntity.getId());

        log.info("[PROFILE-SERVICE] Password changed successfully for user ID: {})", userEntity.getId());
    }

    // ================================================
    //             PRIVATE HELPER METHODS
    //=================================================

    private void updateFailedLoginAttempts(String email) {
        try {
            Optional<User> optionalUser = userRepository.findByEmail(email);
            if(optionalUser.isPresent()){
                User user = optionalUser.get();
                user.incrementFailedLoginAttempts();
                userRepository.save(user);

                if(user.getFailedLoginAttempts() == 4){
                    throw new UnauthorizedException("Account will be locked after 1 more failed attempt");
                }

                if (user.getAccountLocked()) {
                    log.warn("[AUTH-SERVICE] Account locked due to failed attempts: {} (ID: {})",
                            user.getEmail(), user.getId());
                    throw new AccountLockedException("Account is locked for 5 minutes due to failed attempts");
                }
            }
        } catch (UnauthorizedException | AccountLockedException e) {
            // Re-throw
            throw e;
        } catch (Exception e) {
            log.error("[AUTH-SERVICE] Failed to update login attempts: {}", e.getMessage());
            // Don't fail login process if this fails
        }
    }

    private User createUserEntity(RegisterRequest registerRequest) {
        Role defaultRole = roleQueryService.getOrCreateDefaultRole(); // ROLE_USER

        User newUser = User.builder()
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword())) // Hash password
                .emailVerified(false)
                .accountStatus(AccountStatus.PENDING_VERIFICATION)
                .accountLocked(false)
                .failedLoginAttempts(0)
                .build();

        newUser.addRole(defaultRole);

        log.info("[AUTH-SERVICE] User with email: {} is populated successfully", registerRequest.getEmail());
        return newUser;
    }

    private User findUserById(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }
}
