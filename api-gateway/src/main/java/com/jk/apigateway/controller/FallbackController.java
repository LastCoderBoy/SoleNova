package com.jk.apigateway.controller;


import com.jk.commonlibrary.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * Fallback Controller for Circuit Breaker
 * Provides user-friendly error messages when services are down
 *
 * @author LastCoderBoy
 */
@RestController
@RequestMapping("/fallback")
@Slf4j
public class FallbackController {

    /**
     * Fallback for Auth Service
     */
    @RequestMapping(value = "/auth", method = {GET, POST, PUT, DELETE, PATCH})
    public ResponseEntity<ApiResponse<Void>> authServiceFallback() {
        log.error("[FALLBACK] Auth service is currently unavailable");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error(
                        "Authentication service is temporarily unavailable. Please try again later."
                ));
    }

    /**
     * Fallback for Order Service
     */
    @RequestMapping(value = "/order", method = {GET, POST, PUT, DELETE, PATCH})
    public ResponseEntity<ApiResponse<Void>> orderServiceFallback() {
        log.error("[FALLBACK] Order service checkout is currently unavailable");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error(
                        "Checkout is temporarily unavailable. Please try again later."
                ));
    }

    @RequestMapping(value = "/product", method = {GET, POST, PUT, DELETE, PATCH})
    public ResponseEntity<ApiResponse<Void>> productServiceFallback() {
        log.error("[FALLBACK] Product catalog service is unavailable");
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Product catalog is temporarily unavailable."));
    }

    /**
     * Generic fallback
     */
    @RequestMapping(value = "/default", method = {GET, POST, PUT, DELETE, PATCH})
    public ResponseEntity<ApiResponse<Void>> defaultFallback() {
        log.error("[FALLBACK] Service is currently unavailable");

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(
                        "Service is temporarily unavailable. Our team has been notified. Please try again later."
                ));
    }
}
