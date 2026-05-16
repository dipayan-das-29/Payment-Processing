package com.payplatform.auth.api.dto;

import java.util.List;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresInMinutes,
        String userId,
        String username,
        List<String> roles
) {
}
