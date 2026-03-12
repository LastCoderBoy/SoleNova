package com.jk.apigateway.security;

import com.jk.apigateway.dto.JwtClaimsPayload;
import com.jk.commonlibrary.exception.InternalServerException;
import com.jk.commonlibrary.exception.InvalidTokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
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

            log.info("[JWT-PROVIDER] Initialized successfully");
            log.info("[JWT-PROVIDER] Token validity: {} ms ({} minutes)",
                    ACCESS_TOKEN_DURATION_MS, ACCESS_TOKEN_DURATION_MS / 60000);

        } catch (IllegalArgumentException ie) {
            log.error("[JWT-PROVIDER] Invalid JWT secret key: {}", ie.getMessage());
            throw new InvalidTokenException("Invalid JWT secret key");
        } catch (Exception e) {
            log.error("[JWT-PROVIDER] Unexpected error occurred while initializing JWT provider: {}", e.getMessage());
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
            String username = claims.getSubject();
            List<String> roles = extractRoles(claims);

            return Optional.of(new JwtClaimsPayload(userId, username, roles));

        } catch (ExpiredJwtException e) {
            log.warn("[JWT-PROVIDER] Token expired: {}", e.getMessage());
            return Optional.empty();
        } catch (JwtException e) {
            log.warn("[JWT-PROVIDER] Invalid token: {}", e.getMessage());
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
            log.error("[JWT-PROVIDER] Failed to extract roles from token: {}", e.getMessage());
            return List.of(); // Return empty list if roles not found
        }
    }

//
//    /**
//     * Validate JWT token
//     * Checks signature, expiration, and format
//     *
//     * @param token JWT token string
//     * @return true if valid, false otherwise
//     */
//    public boolean validateToken(String token) {
//        try {
//            Jws<Claims> claims = Jwts
//                    .parser()
//                    .verifyWith(secretKey)
//                    .build().parseSignedClaims(token);
//
//            log.info("[JWT-PROVIDER] JWT token is valid for user: {}", claims.getPayload().getSubject());
//            return true;
//        } catch (ExpiredJwtException e) {
//            log.warn("[JWT-PROVIDER] Token expired: {}", e.getMessage());
//            return false;
//        } catch (IllegalArgumentException e) {
//            log.warn("[JWT-PROVIDER] JWT token compact of handler are invalid. {}", e.getMessage());
//            return false;
//        } catch (MalformedJwtException me) {
//            log.warn("[JWT-PROVIDER] Malformed JWT token: {}", me.getMessage());
//            return false;
//        } catch (UnsupportedJwtException e) {
//            log.warn("[JWT-PROVIDER] Unsupported JWT token: {}", e.getMessage());
//            return false;
//        } catch (Exception e) {
//            log.error("[JWT-PROVIDER] Unexpected error occurred while verifying JWT token: {}", e.getMessage());
//            return false;
//        }
//    }

    public Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getExpiration();

        } catch (Exception e) {
            log.error("[JWT-PROVIDER] Failed to extract expiration from token: {}", e.getMessage());
            throw new InvalidTokenException("Failed to extract expiration from token");
        }
    }

    /**
     * Check if token is expired
     *
     * @param token JWT token
     * @return true if expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true; // Consider invalid tokens as expired
        }
    }

    // ==================== HELPER METHODS ====================

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("[AUTH-JWT-PROVIDER] Failed to extract claims: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

}
