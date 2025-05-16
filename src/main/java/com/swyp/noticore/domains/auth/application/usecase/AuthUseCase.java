package com.swyp.noticore.domains.auth.application.usecase;

import static com.swyp.noticore.domains.auth.domain.constants.AuthConstants.ACCESS_COOKIE_KEY;
import static com.swyp.noticore.domains.auth.domain.constants.AuthConstants.REFRESH_COOKIE_KEY;
import static com.swyp.noticore.global.response.code.CommonErrorCode.BAD_REQUEST;

import com.swyp.noticore.domains.auth.application.dto.MemberContext;
import com.swyp.noticore.domains.auth.application.dto.request.GenerateTokenRequest;
import com.swyp.noticore.domains.auth.application.dto.request.LoginRequest;
import com.swyp.noticore.domains.auth.application.dto.response.LoginResponse;
import com.swyp.noticore.domains.auth.application.dto.response.TokenResponse;
import com.swyp.noticore.domains.auth.application.mapper.AuthMapper;
import com.swyp.noticore.domains.auth.domain.service.MemberAuthService;
import com.swyp.noticore.domains.auth.domain.service.TokenService;
import com.swyp.noticore.global.annotation.architecture.UseCase;
import com.swyp.noticore.global.exception.ApplicationException;
import com.swyp.noticore.global.utils.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@UseCase
@RequiredArgsConstructor
@Transactional
public class AuthUseCase {

    private final MemberAuthService memberAuthService;
    private final TokenService tokenService;

    public LoginResponse login(LoginRequest loginRequest, HttpServletResponse response) {

        GenerateTokenRequest generateTokenRequest = memberAuthService.validate(
            loginRequest.email(),
            loginRequest.password()
        );

        TokenResponse tokenResponse = tokenService.generateToken(generateTokenRequest, response);

        return AuthMapper.mapToLoginResponse(
            generateTokenRequest.name(), // TODO: GenerateTokenRequest 내부 데이터(name과 토큰) 분리 필요
            tokenResponse.accessToken(),
            tokenResponse.refreshToken()
        );
    }

    public TokenResponse refresh(MemberContext memberContext, HttpServletRequest request, HttpServletResponse response) {
        GenerateTokenRequest generateTokenRequest = AuthMapper.mapToGenerateTokenRequest(
            memberContext.role(),
            memberContext.memberId(),
            null
        );

        String refreshToken = CookieUtils.extractFromCookie(request, REFRESH_COOKIE_KEY);

        return tokenService.reissueToken(generateTokenRequest, response, refreshToken);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = CookieUtils.extractFromCookie(request, REFRESH_COOKIE_KEY);

        if (refreshToken != null && !refreshToken.isBlank()) {
            tokenService.expireRefreshToken(response, refreshToken);
        } else {
            log.error("유효하지 않은 리프레시 토큰 : {}", refreshToken);
            throw ApplicationException.from(BAD_REQUEST);
        }

        CookieUtils.deleteCookie(response, ACCESS_COOKIE_KEY);
        CookieUtils.deleteCookie(response, REFRESH_COOKIE_KEY);
    }
}
