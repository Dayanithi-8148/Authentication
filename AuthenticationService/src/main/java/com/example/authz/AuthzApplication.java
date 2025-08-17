package com.example.authz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class AuthzApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthzApplication.class, args);
    }
}