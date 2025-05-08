package com.swyp.noticore.domains.incident.application.dto.response;

import java.util.List;
import lombok.Builder;

@Builder
public record IncidentGroupResponse(
    Long id,
    String name,
    List<IncidentGroupMemberResponse> members
) {}