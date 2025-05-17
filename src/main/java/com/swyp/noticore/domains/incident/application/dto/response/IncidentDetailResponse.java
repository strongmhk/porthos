package com.swyp.noticore.domains.incident.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record IncidentDetailResponse(
    Long incidentId,
    String bucket,
    String s3Uuid,
    String rawBody,
    String title,
    LocalDateTime registrationTime,
    LocalDateTime closingTime,
    List<IncidentGroupResponse> groups
) {}
