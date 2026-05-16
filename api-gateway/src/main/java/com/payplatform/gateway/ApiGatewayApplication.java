package com.payplatform.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.payplatform.security.jwt.JwtProperties;

@EnableConfigurationProperties(JwtProperties.class)
@SpringBootApplication(scanBasePackages = "com.payplatform")
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
