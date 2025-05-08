package com.swyp.noticore.domains.incident.application.dto.response;

import lombok.Builder;

@Builder
public record IncidentGroupMemberResponse(
    Long id,
    String name,
    boolean isVerified
) {}