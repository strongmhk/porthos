package com.swyp.noticore.domains.auth.application.dto.response;

import lombok.Builder;

@Builder
public record LoginResponse(
    String name,
    String accessToken,
    String refreshToken
) {

}
