package com.swyp.noticore.domains.auth.domain.service;

import com.swyp.noticore.domains.auth.application.dto.GenerateTokenRequest;
import com.swyp.noticore.domains.auth.application.dto.response.TokenResponse;
import com.swyp.noticore.domains.auth.application.mapper.AuthMapper;
import com.swyp.noticore.global.config.security.jwt.JwtProvider;
import com.swyp.noticore.global.config.security.jwt.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TokenService {

    private final JwtUtils jwtUtils;
    private final JwtProvider jwtProvider;

    public TokenResponse generateToken(GenerateTokenRequest request) {
        String accessToken = jwtProvider.generateAccessToken(request.email(), request.role());
        String refreshToken = jwtProvider.generateRefreshToken(request.email(), request.role(), request.memberId());

        return AuthMapper.mapToTokenResponse(accessToken, refreshToken);
    }

    public TokenResponse reissueToken(GenerateTokenRequest tokenRequest, HttpServletRequest request) {
        jwtUtils.validateRefreshToken(tokenRequest.memberId(), request);
        return generateToken(tokenRequest);
    }
}
