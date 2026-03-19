package com.jk.apigateway.security;

import com.jk.apigateway.redis.RedisService;
import com.jk.commonlibrary.dto.JwtClaimsPayload;
import com.jk.commonlibrary.exception.InvalidTokenException;
import com.jk.commonlibrary.utils.TokenUtils;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.*;

import static com.jk.commonlibrary.constants.AppConstants.*;

@Component
@Slf4j
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final PathMatcher pathMatcher;
    private final JwtTokenProcessor jwtTokenProcessor;
    private final RedisService redisService;
    @Autowired
    public JwtAuthenticationFilter(JwtTokenProcessor jwtTokenProcessor, RedisService redisService) {
        super(Config.class);
        this.jwtTokenProcessor = jwtTokenProcessor;
        this.redisService = redisService;
        this.pathMatcher = new AntPathMatcher();
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().toString();

            log.info("[JWT-AUTH-FILTER] Processing request: {} {}", request.getMethod(), path);

            // Strip any client-supplied internal headers FIRST
            // Prevents header injection attacks where a client pretends
            // to be an authenticated user by forging these headers.
            ServerHttpRequest sanitizedRequest = request.mutate()
                    .headers(headers -> {
                        headers.remove(USER_ID_HEADER);
                        headers.remove(USER_EMAIL_HEADER);
                        headers.remove(USER_ROLES_HEADER);
                    })
                    .build();

            // Replace exchange with sanitized request going forward
            ServerWebExchange sanitizedExchange = exchange.mutate()
                    .request(sanitizedRequest)
                    .build();

            // Note: this check is a safety net.
            // Public routes should NOT have JwtAuthenticationFilter applied in GatewayConfig.
            // This handles edge cases where the filter is applied broadly.
            if (isPublicPath(path, config)) {
                log.info("[JWT-AUTH-FILTER] Public path detected, skipping authentication");
                return chain.filter(sanitizedExchange);
            }

            if (!sanitizedRequest.getHeaders().containsKey(AUTHORIZATION_HEADER)) {
                log.warn("[JWT-AUTH-FILTER] Missing Authorization header for: {}", path);
                return onError(exchange, "Missing Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = sanitizedRequest.getHeaders().getFirst(AUTHORIZATION_HEADER);

            try {
                String token = TokenUtils.validateAndExtractToken(authHeader);

                // Validate token & extract claims
                Optional<JwtClaimsPayload> claimsOpt = jwtTokenProcessor.validateAndExtractClaims(token);
                if (claimsOpt.isEmpty()) {
                    return onError(exchange, "Token has expired or has invalid structure", HttpStatus.UNAUTHORIZED);
                }
                JwtClaimsPayload claims = claimsOpt.get();

                String userId = String.valueOf(claims.userId()); // cannot be null
                String email = claims.email();
                List<String> userRoles = claims.roles();

                // Check if the token is blacklisted in Redis Cache
                return redisService.isTokenBlacklisted(token)
                        .flatMap(isBlacklisted -> {
                            if (isBlacklisted) {
                                log.warn("[JWT-AUTH-FILTER] Blacklisted token attempted for: {}", path);
                                return onError(exchange, "Token has been revoked", HttpStatus.UNAUTHORIZED);
                            }

                            // Check required roles
                            if (!config.getRequiredRoles().isEmpty()) {
                                boolean hasRequiredRole = userRoles.stream()
                                        .anyMatch(role -> config.getRequiredRoles().contains(role));

                                if (!hasRequiredRole) {
                                    log.warn("[JWT-AUTH-FILTER] User {} lacks required roles for {}",
                                            email, path);
                                    return onError(exchange, config.getForbiddenMessage(), HttpStatus.FORBIDDEN);
                                }
                            }

                            log.info("[JWT-AUTH-FILTER] User {} (ID: {}) authenticated successfully",
                                    email, userId);

                            // Add user context headers
                            ServerHttpRequest modifiedRequest = sanitizedRequest.mutate()
                                    .header(USER_ID_HEADER, userId)
                                    .header(USER_EMAIL_HEADER, email)
                                    .header(USER_ROLES_HEADER, String.join(",", userRoles))
                                    .build();

                            return chain.filter(sanitizedExchange.mutate().request(modifiedRequest).build());
                        })
                        .onErrorResume(e -> {
                            log.error("[JWT-AUTH-FILTER] Unexpected reactive error: {}", e.getMessage(), e);
                            return onError(sanitizedExchange, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
                        });

            } catch (InvalidTokenException e) {
                log.warn("[JWT-AUTH-FILTER] Invalid token: {}", e.getMessage());
                return onError(sanitizedExchange, e.getMessage(), HttpStatus.UNAUTHORIZED);
            } catch (Exception e) {
                log.error("[JWT-AUTH-FILTER] Unexpected error: {}", e.getMessage(), e);
                return onError(sanitizedExchange, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String errorJson = String.format(
                "{\"success\": false, \"message\": \"%s\", \"data\": null}",
                message
        );

        return response.writeWith(Mono.just(response.bufferFactory().wrap(errorJson.getBytes())));

    }

    private boolean isPublicPath(String path, Config config) {
        boolean isGlobalPath = PUBLIC_PATHS.stream().anyMatch(
                pattern -> pathMatcher.match(pattern, path));

        boolean isRoutePublic = config.getPublicPaths() != null &&
                !config.getPublicPaths().isEmpty() &&
                config.getPublicPaths().stream().anyMatch(pattern ->
                        pathMatcher.match(pattern, path));

        return isGlobalPath || isRoutePublic;
    }


    /**
     * Configuration class for per-route filter customization
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Config {

        /**
         * Enable/disable JWT authentication for this route
         */
        @Builder.Default
        private boolean enabled = true;

        /**
         * Additional public paths for this specific route (supports wildcards)
         */
        @Builder.Default
        private List<String> publicPaths = new ArrayList<>();

        /**
         * Required roles for accessing this route
         * If empty, any authenticated user can access
         */
        @Builder.Default
        private List<String> requiredRoles = new ArrayList<>();

        /**
         * Custom headers to add to downstream requests
         */
        @Builder.Default
        private Map<String, String> customHeaders = new HashMap<>();

        /**
         * Custom unauthorized message
         */
        @Builder.Default
        private String unauthorizedMessage = "Unauthorized access";

        /**
         * Custom forbidden message
         */
        @Builder.Default
        private String forbiddenMessage = "Insufficient permissions";
    }
}
