package com.swyp.noticore.domains.auth.presentation;

import static com.swyp.noticore.domains.auth.domain.constants.AuthConstants.ACCESS_COOKIE_KEY;
import static com.swyp.noticore.domains.auth.domain.constants.AuthConstants.REFRESH_COOKIE_KEY;

import com.swyp.noticore.domains.auth.application.dto.MemberContext;
import com.swyp.noticore.domains.auth.application.dto.request.LoginRequest;
import com.swyp.noticore.domains.auth.application.dto.response.LoginInfoResponse;
import com.swyp.noticore.domains.auth.application.dto.response.LoginResponse;
import com.swyp.noticore.domains.auth.application.dto.response.TokenResponse;
import com.swyp.noticore.domains.auth.application.mapper.AuthMapper;
import com.swyp.noticore.domains.auth.application.usecase.AuthUseCase;
import com.swyp.noticore.global.constants.SameSitePolicy;
import com.swyp.noticore.global.utils.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthUseCase authUseCase;

    @Value("${jwt.refresh-expiration}")
    private int refreshExpiration;

    @Value("${jwt.access-expiration}")
    private int accessExpiration;

    @PostMapping("/login")
    public ResponseEntity<LoginInfoResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        LoginResponse loginResponse = authUseCase.login(request, response);

        ResponseCookie accessTokenCookie = CookieUtils.createCookie(ACCESS_COOKIE_KEY, loginResponse.accessToken(), accessExpiration, true, true, SameSitePolicy.NONE);
        ResponseCookie refreshTokenCookie = CookieUtils.createCookie(REFRESH_COOKIE_KEY, loginResponse.refreshToken(), refreshExpiration, true, true, SameSitePolicy.NONE);

        response.addHeader("Set-Cookie", accessTokenCookie.toString());
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());

        return ResponseEntity.ok(
            AuthMapper.mapToLoginInfoResponse(loginResponse.name())
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity reissue(@AuthenticationPrincipal MemberContext memberContext, HttpServletRequest request, HttpServletResponse response) {
        log.info("memberContext: {}", memberContext);

        TokenResponse tokenResponse = authUseCase.refresh(memberContext, request, response);

        ResponseCookie accessTokenCookie = CookieUtils.createCookie(ACCESS_COOKIE_KEY, tokenResponse.accessToken(), accessExpiration, true, true, SameSitePolicy.NONE);
        ResponseCookie refreshTokenCookie = CookieUtils.createCookie(REFRESH_COOKIE_KEY, tokenResponse.refreshToken(), refreshExpiration, true, true, SameSitePolicy.NONE);

        response.addHeader("Set-Cookie", accessTokenCookie.toString());
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());

        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity logout(HttpServletRequest request, HttpServletResponse response) {
        authUseCase.logout(request, response);

        return new ResponseEntity(HttpStatus.OK);
    }
}
