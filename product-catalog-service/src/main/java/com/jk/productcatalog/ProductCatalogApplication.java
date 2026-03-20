package com.jk.productcatalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProductCatalogApplication {
    public static void main(String[] args) {
        System.out.println("Product Catalog Service Started");

        SpringApplication.run(ProductCatalogApplication.class, args);
    }
}
