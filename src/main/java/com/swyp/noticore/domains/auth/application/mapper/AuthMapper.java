package com.swyp.noticore.domains.auth.application.mapper;

import com.swyp.noticore.domains.auth.application.dto.GenerateTokenRequest;
import com.swyp.noticore.domains.auth.application.dto.response.TokenResponse;
import com.swyp.noticore.domains.member.domain.constant.Role;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    public static TokenResponse mapToTokenResponse(String accessToken, String refreshToken) {
        return TokenResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();
    }

    public static GenerateTokenRequest mapToGenerateTokenRequest(String email, Role role, Long memberId) {
        return GenerateTokenRequest.builder()
            .email(email)
            .role(role)
            .memberId(memberId)
            .build();
    }
}
