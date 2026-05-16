package com.payplatform.gateway.config;

import com.payplatform.gateway.filter.JwtAuthGatewayFilterFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder,
                                           JwtAuthGatewayFilterFactory jwtAuthGatewayFilterFactory) {
        return builder.routes()
                .route("auth-service-route", route -> route
                        .path("/auth/**")
                        .uri("http://localhost:8085"))
                .route("payment-service-route", route -> route
                        .path("/v1/payments/**")
                        .filters(filter -> filter
                                .filter(jwtAuthGatewayFilterFactory.apply(new JwtAuthGatewayFilterFactory.Config())))
                        .uri("http://localhost:8081"))
                .route("payment-health-route", route -> route
                        .path("/payment-service/health/**")
                        .uri("http://localhost:8081"))
                .build();
    }
}