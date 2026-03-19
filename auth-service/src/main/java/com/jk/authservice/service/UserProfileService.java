package com.jk.authservice.service;

import com.jk.authservice.dto.request.ChangeEmailRequest;
import com.jk.authservice.dto.request.ChangePasswordRequest;
import com.jk.authservice.dto.request.UpdateProfileRequest;
import com.jk.authservice.dto.response.UserProfileResponse;
import com.jk.authservice.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface UserProfileService {

    UserProfileResponse getProfile(Long id);

    UserProfileResponse updateProfile(UpdateProfileRequest request, Long id);

    void changePassword(ChangePasswordRequest request, Long id, HttpServletResponse httpResponse);

    void requestEmailChange(ChangeEmailRequest request, Long id);

    void logout(Long id, HttpServletResponse response, HttpServletRequest request);

    void logoutAll(Long id, HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    User findUserById(Long id);
}
