package com.swyp.noticore.domains.auth.presentation;

import com.swyp.noticore.domains.auth.application.dto.request.LoginRequest;
import com.swyp.noticore.domains.auth.application.dto.response.TokenResponse;
import com.swyp.noticore.domains.auth.application.usecase.AuthUseCase;
import com.swyp.noticore.global.response.ApplicationResponse;
import com.swyp.noticore.global.utils.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthUseCase authUseCase;

    private static final String ACCESS_KEY = "accessToken";
    private static final String REFRESH_KEY = "refreshToken";

    @PostMapping("/login")
    public ApplicationResponse<Void> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        TokenResponse tokenResponse = authUseCase.login(request);

        Cookie accessCookie = CookieUtils.createCookie(ACCESS_KEY, tokenResponse.accessToken(), 24 * 60 * 60, true);
        Cookie refreshCookie = CookieUtils.createCookie(REFRESH_KEY, tokenResponse.refreshToken(), 24 * 60 * 60, true);
        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        return ApplicationResponse.onSuccess();
    }
}
