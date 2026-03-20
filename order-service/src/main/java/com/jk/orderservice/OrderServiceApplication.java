package com.jk.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OrderServiceApplication {
    public static void main(String[] args) {
        System.out.println("Order Service Started");

        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
