package com.jk.authservice.service.email;

import com.jk.authservice.entity.EmailToken;
import com.jk.authservice.entity.User;

public interface EmailService {

    void sendVerificationEmail(User user, EmailToken emailToken);

    void clickVerificationEmailLink(String token);

    void resendVerificationEmail(String email);

    void sendForgotPasswordEmail(User user, EmailToken emailToken);
}
