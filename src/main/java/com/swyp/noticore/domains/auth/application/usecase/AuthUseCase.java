package com.swyp.noticore.domains.auth.application.usecase;

import static com.swyp.noticore.domains.auth.domain.constants.AuthConstants.REFRESH_COOKIE_KEY;

import com.swyp.noticore.domains.auth.application.dto.GenerateTokenRequest;
import com.swyp.noticore.domains.auth.application.dto.MemberContext;
import com.swyp.noticore.domains.auth.application.dto.request.LoginRequest;
import com.swyp.noticore.domains.auth.application.dto.response.TokenResponse;
import com.swyp.noticore.domains.auth.application.mapper.AuthMapper;
import com.swyp.noticore.domains.auth.domain.service.MemberAuthService;
import com.swyp.noticore.domains.auth.domain.service.TokenService;
import com.swyp.noticore.global.annotation.architecture.UseCase;
import com.swyp.noticore.global.utils.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class AuthUseCase {

    private final MemberAuthService memberAuthService;
    private final TokenService tokenService;

    @Transactional
    public TokenResponse login(LoginRequest loginRequest, HttpServletResponse response) {
        GenerateTokenRequest generateTokenRequest = memberAuthService.validate(
            loginRequest.email(),
            loginRequest.password()
        );
        return tokenService.generateToken(generateTokenRequest, response);
    }

    @Transactional
    public TokenResponse refresh(MemberContext memberContext, HttpServletRequest request, HttpServletResponse response) {
        GenerateTokenRequest generateTokenRequest = AuthMapper.mapToGenerateTokenRequest(
            memberContext.role(),
            memberContext.memberId()
        );

        String refreshToken = CookieUtils.extractFromCookie(request, REFRESH_COOKIE_KEY);

        return tokenService.reissueToken(generateTokenRequest, response, refreshToken);
    }
}
