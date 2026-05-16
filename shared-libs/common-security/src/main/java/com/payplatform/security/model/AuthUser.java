package com.payplatform.security.model;

import java.util.List;

public record AuthUser(
        String userId,
        String username,
        List<String> roles
) {
}
