package com.jk.authservice.service.email.impl;

import com.jk.authservice.dto.request.ResetPasswordRequest;
import com.jk.authservice.entity.EmailToken;
import com.jk.authservice.entity.User;
import com.jk.authservice.enums.AccountStatus;
import com.jk.authservice.enums.TokenType;
import com.jk.authservice.repository.UserRepository;
import com.jk.authservice.service.email.EmailService;
import com.jk.authservice.service.email.EmailTokenService;
import com.jk.commonlibrary.exception.InternalServerException;
import com.jk.commonlibrary.exception.ResourceNotFoundException;
import com.jk.commonlibrary.exception.ValidationException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static com.jk.commonlibrary.constants.AppConstants.EMAIL_VERIFICATION_EXPIRY_MINUTES;
import static com.jk.commonlibrary.constants.AppConstants.PASSWORD_RESET_EXPIRY_MINUTES;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final EmailTokenService emailTokenService;
    private final UserRepository userRepository;

    @Value("${spring.mail.username}")
    private String emailFrom;

    @Value("${email.verification.url}")
    private String emailVerificationUrl;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    private final String appName = "SoleNova";

    @Async("taskExecutor")
    public void sendVerificationEmail(User user, EmailToken emailToken) {
        try {
            String subject = appName + " - Verify Your Email Address";
            String verificationUrl = buildVerificationUrl(emailToken.getToken());

            String htmlTemplate = loadHtmlTemplate("templates/email/verify-email.html");
            String cssStyles = loadCssStyles("static/email/css/email-styles.css");

            Map<String, String> placeholders = Map.of(
                    "{{FIRST_NAME}}", user.getFirstName(),
                    "{{LAST_NAME}}", user.getLastName() != null ? user.getLastName() : "",
                    "{{VERIFICATION_URL}}", verificationUrl,
                    "{{APP_NAME}}", appName,
                    "{{EXPIRY_MINUTES}}", String.valueOf(EMAIL_VERIFICATION_EXPIRY_MINUTES)
            );

            String htmlContent = replacePlaceholders(htmlTemplate, placeholders);
            htmlContent = inlineCss(htmlContent, cssStyles);

            sendEmail(user.getEmail(), subject, htmlContent);

            log.info("[EMAIL-SERVICE] Verification email sent to: {}", user.getEmail());

        } catch (Exception e) {
            log.error("[EMAIL-SERVICE] Failed to send verification email to {}: {}",
                    user.getEmail(), e.getMessage(), e);
        }
    }

    @Transactional
    public void clickVerificationEmailLink(String token) {
        User user = emailTokenService.verifyToken(token, TokenType.EMAIL_VERIFICATION);

        if (user.getEmailVerified()) {
            log.info("[EMAIL-SERVICE] Email already verified for user: {}", user.getId());
            return;
        }

        user.setEmailVerified(true);
        user.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(user);

        log.info("[EMAIL-SERVICE] Email verified successfully for user: {}", user.getId());
    }

    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("[EMAIL-SERVICE] User not found with email: {}", email);
                    return new ResourceNotFoundException("User not found");
                });

        if (user.getEmailVerified()) {
            throw new ValidationException("Email is already verified");
        }

        // Create new token
        emailTokenService.revokeUserTokens(user.getId(), TokenType.EMAIL_VERIFICATION);
        EmailToken emailToken = emailTokenService.createEmailToken(user, TokenType.EMAIL_VERIFICATION);

        // Send email
        sendVerificationEmail(user, emailToken);

        log.info("[EMAIL-SERVICE] Verification email resent to user: {}", email);
    }

    @Async("taskExecutor")
    @Override
    public void sendForgotPasswordEmail(User user, EmailToken emailToken) {
        try {
            String subject = appName + " - Reset Your Password";
            String resetUrl = buildResetPasswordUrl(emailToken.getToken());

            String htmlTemplate = loadHtmlTemplate("templates/email/forgot-password.html");
            String cssStyles    = loadCssStyles("static/email/css/email-styles.css");

            Map<String, String> placeholders = Map.of(
                    "{{FIRST_NAME}}",      user.getFirstName(),
                    "{{RESET_URL}}",       resetUrl,
                    "{{APP_NAME}}",        appName,
                    "{{EXPIRY_MINUTES}}", String.valueOf(PASSWORD_RESET_EXPIRY_MINUTES)
            );

            String htmlContent = replacePlaceholders(htmlTemplate, placeholders);
            htmlContent = inlineCss(htmlContent, cssStyles);

            sendEmail(user.getEmail(), subject, htmlContent);

            log.info("[EMAIL-SERVICE] Password reset email sent to: {}", user.getEmail());

        } catch (Exception e) {
            // Never rethrow inside @Async — exception would be swallowed anyway
            log.error("[EMAIL-SERVICE] Failed to send password reset email to {}: {}",
                    user.getEmail(), e.getMessage(), e);
        }
    }

    // ========= Private Helper Methods =========

    private void sendEmail(String to, String subject, String content)
            throws MessagingException, UnsupportedEncodingException {

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setFrom(emailFrom, appName + " Support");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);

        javaMailSender.send(mimeMessage);
    }

    private String buildVerificationUrl(String token) {
        // Full URL for email link
        return String.format("%s/verify-email?token=%s", emailVerificationUrl, token);
    }

    private String buildResetPasswordUrl(String token) {
        // Points to FRONTEND, not API — frontend extracts token and calls POST /reset-password
        return String.format("%s/reset-password?token=%s", frontendBaseUrl, token);
    }

    private String loadHtmlTemplate(String templatePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(templatePath);
        try (InputStream inputStream = resource.getInputStream()) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        }
    }

    private String loadCssStyles(String cssPath) throws IOException {
        ClassPathResource resource = new ClassPathResource(cssPath);
        try (InputStream inputStream = resource.getInputStream()) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        }
    }

    private String replacePlaceholders(String template, Map<String, String> placeholders) {
        String result = template;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private String inlineCss(String html, String css) {
        return html.replace("</head>", "<style>" + css + "</style></head>");
    }
}
