package com.swyp.noticore.domains.auth.application.dto;

import com.swyp.noticore.domains.member.domain.constant.Role;
import lombok.Builder;

@Builder
public record GenerateTokenRequest(
    String email,
    Role role,
    Long memberId
) {

}
