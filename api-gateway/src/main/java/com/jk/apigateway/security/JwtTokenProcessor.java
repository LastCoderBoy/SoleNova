package com.jk.apigateway.security;

import com.jk.commonlibrary.dto.JwtClaimsPayload;
import com.jk.commonlibrary.exception.InternalServerException;
import com.jk.commonlibrary.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.Optional;

import static com.jk.commonlibrary.constants.AppConstants.*;

@Component
@Slf4j
public class JwtTokenProcessor {

    private SecretKey secretKey;

    @Value( "${jwt.secret}")
    private String key;

    @PostConstruct
    public void init() {
        try{
            byte[] decodedBytes = Decoders.BASE64.decode(key);
            this.secretKey = Keys.hmacShaKeyFor(decodedBytes); // throws WeakKeyException if the key is too weak

            log.info("[JWT-PROCESSOR] Initialized successfully");
            log.info("[JWT-PROCESSOR] Token validity: {} ms ({} minutes)",
                    ACCESS_TOKEN_DURATION_MS, ACCESS_TOKEN_DURATION_MS / 60000);

        } catch (IllegalArgumentException ie) {
            log.error("[JWT-PROCESSOR] Invalid JWT secret key: {}", ie.getMessage());
            throw new InvalidTokenException("Invalid JWT secret key");
        } catch (Exception e) {
            log.error("[JWT-PROCESSOR] Unexpected error occurred while initializing JWT provider: {}", e.getMessage());
            throw new InternalServerException("Unexpected error occurred!");
        }
    }

    /**
     * Parse token ONCE and return all claims needed by the gateway.
     * Returns empty Optional if token is invalid/expired.
     */
    public Optional<JwtClaimsPayload> validateAndExtractClaims(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Long userId = extractUserId(claims);
            String email = claims.getSubject();
            List<String> roles = extractRoles(claims);

            return Optional.of(new JwtClaimsPayload(userId, email, roles));

        } catch (ExpiredJwtException e) {
            log.warn("[JWT-PROCESSOR] Token expired: {}", e.getMessage());
            return Optional.empty();
        } catch (JwtException e) {
            log.warn("[JWT-PROCESSOR] Invalid token: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private Long extractUserId(Claims claims) {
        Object userIdKey = claims.get(JWT_CLAIM_USER_ID);

        if (userIdKey instanceof Integer) {
            return ((Integer) userIdKey).longValue();
        } else if (userIdKey instanceof Long) {
            return (Long) userIdKey;
        } else if (userIdKey != null) {
            return Long.parseLong(userIdKey.toString());
        }

        return null;
    }

    private List<String> extractRoles(Claims claims) {
        try {
            Object roles = claims.get(JWT_CLAIM_ROLES);

            if (roles instanceof List) {
                return (List<String>) roles;
            }

            return List.of();
        } catch (Exception e) {
            log.error("[JWT-PROCESSOR] Failed to extract roles from token: {}", e.getMessage());
            return List.of(); // Return empty list if roles not found
        }
    }
}
