package com.swyp.noticore.domains.auth.application.usecase;

import com.swyp.noticore.domains.auth.application.dto.GenerateTokenRequest;
import com.swyp.noticore.domains.auth.application.dto.request.LoginRequest;
import com.swyp.noticore.domains.auth.application.dto.response.TokenResponse;
import com.swyp.noticore.domains.auth.domain.service.MemberAuthService;
import com.swyp.noticore.domains.auth.domain.service.TokenService;
import com.swyp.noticore.global.annotation.architecture.UseCase;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class AuthUseCase {

    private final MemberAuthService memberAuthService;
    private final TokenService tokenService;

    public TokenResponse login(LoginRequest loginRequest) {
        GenerateTokenRequest generateTokenRequest = memberAuthService.validate(loginRequest.email(), loginRequest.password());
        return tokenService.generateToken(generateTokenRequest);
    }
}
