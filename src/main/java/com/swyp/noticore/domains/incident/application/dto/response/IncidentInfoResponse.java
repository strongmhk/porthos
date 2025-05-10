package com.swyp.noticore.domains.incident.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.swyp.noticore.domains.incident.application.dto.response.IncidentGroupResponse;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentInfoResponse {
    private Long id;
    private String title;
    private LocalDateTime registrationTime;
    private LocalDateTime closingTime;
    private List<IncidentGroupResponse> groups;
}
