package com.jk.apigateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Application-wide configuration properties
 * Binds to 'app.*' properties in application.yml
 */
@Configuration
@ConfigurationProperties(prefix = "app")
@EnableConfigurationProperties(AppProperties.class)
@Data
public class AppProperties {

    private Cors cors = new Cors();

    @Data
    public static class Cors {
        private List<String> allowedOrigins;
        private List<String> allowedMethods;
        private boolean allowCredentials = true;
        private long maxAge = 3600;
    }
}
