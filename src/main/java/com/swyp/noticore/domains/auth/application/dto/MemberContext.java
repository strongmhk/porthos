package com.swyp.noticore.domains.auth.application.dto;

import com.swyp.noticore.domains.member.domain.constant.Role;
import lombok.Builder;

@Builder
public record MemberContext(
    Role role,
    Long memberId,
    String email
) {

}
