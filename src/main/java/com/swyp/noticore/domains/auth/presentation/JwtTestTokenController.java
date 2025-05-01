package com.swyp.noticore.domains.auth.presentation;

import com.swyp.noticore.global.config.security.jwt.JwtUtils;
import com.swyp.noticore.global.config.security.jwt.constant.TokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/test-token")
@RequiredArgsConstructor
// @Profile("dev")
public class JwtTestTokenController {

    private final JwtUtils jwtUtils;

    @GetMapping(produces = "text/plain")
    public String generateTestToken() {
        String token = jwtUtils.generateToken(
            "swagger@example.com",
            "ROLE_ADMIN",
            TokenType.ACCESS,
            3600_000L
        );
        return "Bearer " + token;
    }
}
