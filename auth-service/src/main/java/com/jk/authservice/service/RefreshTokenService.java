package com.jk.authservice.service;

import com.jk.authservice.entity.RefreshToken;
import com.jk.authservice.entity.User;
import jakarta.servlet.http.HttpServletResponse;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user, String clientIP, String userAgent);

    RefreshToken findByToken(String token);

    RefreshToken verifyRefreshToken(String token, HttpServletResponse response);

    RefreshToken rotateRefreshToken(RefreshToken oldToken, String clientIP, String userAgent);

    void revokeRefreshToken(String token);

    void revokeAllRefreshTokensAsync(Long userId);
}
