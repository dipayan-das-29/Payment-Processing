package com.payplatform.security.jwt;

import java.util.Optional;

public final class BearerTokenExtractor {
    private static final String PREFIX = "Bearer ";

    private BearerTokenExtractor() {
    }

    public static Optional<String> extract(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return Optional.empty();
        }
        if (!authorizationHeader.startsWith(PREFIX)) {
            return Optional.empty();
        }
        return Optional.of(authorizationHeader.substring(PREFIX.length()));
    }
}