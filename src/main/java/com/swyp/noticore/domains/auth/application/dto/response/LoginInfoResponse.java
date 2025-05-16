package com.swyp.noticore.domains.auth.application.dto.response;

import lombok.Builder;

@Builder
public record LoginInfoResponse(
    String name
) {

}
