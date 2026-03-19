package com.jk.authservice.service.email;

import com.jk.authservice.entity.EmailToken;
import com.jk.authservice.entity.User;
import com.jk.authservice.enums.TokenType;

public interface EmailTokenService {

    EmailToken createEmailToken(User user, TokenType tokenType);

    void revokeUserTokens(Long userId, TokenType tokenType);

    User verifyToken(String token, TokenType tokenType);
}
