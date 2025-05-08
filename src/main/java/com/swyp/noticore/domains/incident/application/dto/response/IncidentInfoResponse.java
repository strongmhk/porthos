package com.swyp.noticore.domains.incident.application.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IncidentInfoResponse {
    private Long id;
    private LocalDateTime registrationTime;
    private LocalDateTime closingTime;
    private String title;
    private List<String> groupNames = new ArrayList<>();
    private long verifiedCount;
    private long totalMemberCount;
}
