package com.payplatform.gateway.filter;

import com.payplatform.security.jwt.BearerTokenExtractor;
import com.payplatform.security.jwt.JwtTokenService;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class JwtAuthGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtAuthGatewayFilterFactory.Config> {

    private final JwtTokenService jwtTokenService;

    public JwtAuthGatewayFilterFactory(JwtTokenService jwtTokenService) {
        super(Config.class);
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> BearerTokenExtractor.extract(
                exchange.getRequest().getHeaders().getFirst("Authorization"))
                .map(token -> validateAndContinue(exchange, chain, token))
                .orElseGet(() -> unauthorized(exchange, "Missing bearer token"));
    }

    private Mono<Void> validateAndContinue(ServerWebExchange exchange, GatewayFilterChain chain, String token) {
        try {
            var authUser = jwtTokenService.toAuthUser(token);
            String correlationId = exchange.getRequest().getHeaders().getFirst("X-Correlation-Id");
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = UUID.randomUUID().toString();
            }

            var mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", authUser.userId())
                    .header("X-Username", authUser.username())
                    .header("X-Roles", String.join(",", authUser.roles()))
                    .header("X-Correlation-Id", correlationId)
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (Exception ex) {
            return unauthorized(exchange, "Invalid JWT token");
        }
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
        String json = String.format("{\"error\":\"%s\"}", message);
        byte[] body = json.getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(body)));
    }

    public static class Config {
    }
}
