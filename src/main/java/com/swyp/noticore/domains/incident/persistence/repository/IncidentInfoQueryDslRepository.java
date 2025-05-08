package com.swyp.noticore.domains.incident.persistence.repository;

import com.swyp.noticore.domains.incident.application.dto.response.IncidentInfoResponse;
import java.util.List;

public interface IncidentInfoQueryDslRepository {

    List<IncidentInfoResponse> findIncidentInfosByCompletion(boolean completion);
}
