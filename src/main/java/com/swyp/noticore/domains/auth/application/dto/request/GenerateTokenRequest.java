package com.swyp.noticore.domains.auth.application.dto.request;

import com.swyp.noticore.domains.member.domain.constant.Role;
import lombok.Builder;

@Builder
public record GenerateTokenRequest(
    Long memberId,
    Role role
) {

}
