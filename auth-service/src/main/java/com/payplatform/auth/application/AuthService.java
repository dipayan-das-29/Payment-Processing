package com.payplatform.auth.application;

import com.payplatform.auth.api.dto.LoginRequest;
import com.payplatform.auth.api.dto.LoginResponse;
import com.payplatform.security.jwt.JwtProperties;
import com.payplatform.security.jwt.JwtTokenService;
import com.payplatform.security.model.AuthUser;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {
    private static final String DEMO_USERNAME = "demo";
    private static final String DEMO_PASSWORD = "demo123";

    private final JwtTokenService jwtTokenService;
    private final JwtProperties jwtProperties;

    public AuthService(JwtTokenService jwtTokenService, JwtProperties jwtProperties) {
        this.jwtTokenService = jwtTokenService;
        this.jwtProperties = jwtProperties;
    }

    public LoginResponse login(LoginRequest request) {
        if (!DEMO_USERNAME.equals(request.username()) || !DEMO_PASSWORD.equals(request.password())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        AuthUser user = new AuthUser("user-demo-1", DEMO_USERNAME, List.of("USER"));
        String token = jwtTokenService.generateToken(user);
        return new LoginResponse(
                token,
                "Bearer",
                jwtProperties.getExpirationMinutes(),
                user.userId(),
                user.username(),
                user.roles()
        );
    }
}
