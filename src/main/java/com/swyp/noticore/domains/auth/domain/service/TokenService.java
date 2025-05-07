package com.swyp.noticore.domains.auth.domain.service;

import com.swyp.noticore.domains.auth.application.dto.request.GenerateTokenRequest;
import com.swyp.noticore.domains.auth.application.dto.response.TokenResponse;
import com.swyp.noticore.domains.auth.application.mapper.AuthMapper;
import com.swyp.noticore.global.config.security.jwt.JwtProvider;
import com.swyp.noticore.global.config.security.jwt.JwtUtils;
import com.swyp.noticore.global.config.security.jwt.constant.TokenType;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TokenService {

    private final JwtUtils jwtUtils;
    private final JwtProvider jwtProvider;

    public TokenResponse generateToken(GenerateTokenRequest request, HttpServletResponse response) {
        String accessToken = jwtProvider.generateAccessToken(request.memberId(), request.role());
        String refreshToken = jwtProvider.generateRefreshToken(request.memberId(), request.role());

        jwtUtils.saveAuthentication(response, accessToken);

        return AuthMapper.mapToTokenResponse(accessToken, refreshToken);
    }

    public TokenResponse reissueToken(GenerateTokenRequest tokenRequest, HttpServletResponse response, String refreshToken) {
        jwtUtils.validateRefreshToken(tokenRequest.memberId(), refreshToken);
        return generateToken(tokenRequest, response);
    }

    public void expireRefreshToken(HttpServletResponse response, String refreshToken) {
        Long memberId = jwtUtils.getMemberIdFromToken(response, refreshToken, TokenType.REFRESH);
        jwtProvider.expireRefreshToken(memberId);
    }
}
