package com.swyp.noticore.domains.incident.application.dto.response;

import java.util.List;
import lombok.Builder;

@Builder
public record IncidentDetailResponse(
    Long incidentId,
    String bucket,
    String s3Uuid,
    String title,
    List<IncidentGroupResponse> groups
) {}