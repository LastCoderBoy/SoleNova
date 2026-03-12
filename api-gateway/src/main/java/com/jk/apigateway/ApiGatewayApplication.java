package com.jk.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient // Allowing to register itself with Eureka and discover other services
public class ApiGatewayApplication {
    public static void main(String[] args) {

        SpringApplication.run(ApiGatewayApplication.class, args);

        System.out.println("""
            
            ╔════════════════════════════════════════════════════════════╗
            ║                 API GATEWAY STARTED                        ║
            ╠════════════════════════════════════════════════════════════╣
            ║  Gateway URL: http://localhost:8080                        ║
            ║  Eureka Server: http://localhost:8761                      ║
            ║  Actuator Health: http://localhost:8080/actuator/health    ║
            ╠════════════════════════════════════════════════════════════╣
            ║  Waiting for services to register...                       ║
            ║                                                            ║
            ╚════════════════════════════════════════════════════════════╝
            
            """);
    }
}
