package com.jk.authservice.service;

import com.jk.authservice.dto.request.LoginRequest;
import com.jk.authservice.dto.request.RegisterRequest;
import com.jk.authservice.dto.request.ResetPasswordRequest;
import com.jk.authservice.dto.response.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthenticationService {

    AuthResponse register(RegisterRequest registerRequest,
                          HttpServletResponse response,
                          HttpServletRequest request);

    AuthResponse login(LoginRequest loginRequest,
                       HttpServletRequest request,
                       HttpServletResponse response);

    AuthResponse refreshJwtTokens(HttpServletRequest request, HttpServletResponse response);

    void forgotPassword(String email);

    void resetPassword(String emailToken, ResetPasswordRequest request, HttpServletResponse httpResponse);
}
