package com.jk.apigateway.config;

import com.jk.apigateway.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import java.util.List;

import static com.jk.commonlibrary.constants.AppConstants.*;

/**
 * API Gateway Route Configuration
 * Defines routing rules for all microservices
 */
@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // ==========================================
                // SWAGGER/OPENAPI DOCUMENTATION ROUTES
                // (Public - No JWT required)
                // ==========================================

                // Auth Service Swagger
                .route("auth-service-swagger", r -> r
                        .path("/auth-service/v3/api-docs", "/auth-service/swagger-ui/**")
                        .filters(f -> f.rewritePath(
                                "/auth-service/(?<segment>.*)",
                                "/${segment}"))
                        .uri("lb://" + AUTH_SERVICE))

                // Will implement Swagger later for other services


                // ==========================================
                // AUTH ADMIN — must come before auth-service route
                // ==========================================
                .route("auth-admin", r -> r
                        .path(AUTH_PATH + "/admin/**")
                        .filters(f -> f
                                .filter(jwtFilter.apply(
                                        JwtAuthenticationFilter.Config.builder()
                                                .requiredRoles(List.of("ROLE_ADMIN"))
                                                .build()
                                ))
                                .circuitBreaker(c -> c
                                        .setName("authServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/auth"))
                        )
                        .uri("lb://" + AUTH_SERVICE))

                // ==========================================
                // PRODUCT CATALOG ADMIN
                // ==========================================
                .route("product-catalog-admin", r -> r
                        .path(PRODUCT_CATALOG_PATH + "/admin/**")
                        .filters(f -> f
                                .filter(jwtFilter.apply(
                                        JwtAuthenticationFilter.Config.builder()
                                                .requiredRoles(List.of("ROLE_ADMIN"))
                                                .build()
                                ))
                                .circuitBreaker(c -> c
                                        .setName("productCatalogServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/product"))
                        )
                        .uri("lb://" + PRODUCT_CATALOG_SERVICE))

                // ==========================================
                // ORDER ADMIN
                // ==========================================
                .route("order-admin", r -> r
                        .path(ORDER_PATH + "/admin/**")
                        .filters(f -> f
                                .filter(jwtFilter.apply(
                                        JwtAuthenticationFilter.Config.builder()
                                                .requiredRoles(List.of("ROLE_ADMIN"))
                                                .build()
                                ))
                                .circuitBreaker(c -> c
                                        .setName("orderServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/order"))
                        )
                        .uri("lb://" + ORDER_SERVICE))

                // ==========================================
                // PUBLIC PATH (No JWT required)
                // ==========================================
                .route("auth-public", r -> r
                        .path(PUBLIC_PATHS.toArray(new String[0]))
                        .filters(f -> f
                                .circuitBreaker(c -> c
                                        .setName("authServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/auth"))
                        )
                        .uri("lb://" + AUTH_SERVICE))

                // ==========================================
                // AUTH SERVICE - Authenticated routes (User Profiles)
                // ==========================================
                .route("auth-service", r -> r
                        .path(AUTH_PATH + "/user-profile/**")
                        .filters(f -> f
                                .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                                .circuitBreaker(c -> c
                                        .setName("authServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/auth"))
                        )
                        .uri("lb://" + AUTH_SERVICE))


                // ==========================================
                // PRODUCT CATALOG SERVICE - Public endpoint
                // ==========================================
                .route("product-catalog-public", r -> r
                        .path(PRODUCT_CATALOG_PATH + "/**")
                        .filters(f -> f
                                .circuitBreaker(c -> c
                                        .setName("productCatalogServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/product")))
                        .uri("lb://" + PRODUCT_CATALOG_SERVICE))

                // ==========================================
                // ORDER SERVICE
                // ==========================================
                .route("order-service", r -> r
                        .path(ORDER_PATH + "/**")
                        .filters(f -> f
                                .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                                .circuitBreaker(c -> c
                                        .setName("orderServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/order"))
                        )
                        .uri("lb://" + ORDER_SERVICE))


                // ==========================================
                // CATCH-ALL / DEFAULT (MUST remain last — catches all unmatched routes)
                // ==========================================
                .route("default-route", r -> r
                        .path("/**")
                        .and()
                        .not(p -> p.path("/fallback/**"))   // exclude fallback endpoints
                        .uri("forward:/fallback/default"))

                .build();
    }
}
