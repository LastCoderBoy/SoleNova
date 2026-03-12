package com.jk.apigateway.dto;


import java.util.List;

public record JwtClaimsPayload(Long userId, String username, List<String> roles) {}
