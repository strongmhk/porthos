package com.swyp.noticore.domains.incident.domain.service;

import com.swyp.noticore.domains.incident.application.dto.response.IncidentInfoResponse;
import com.swyp.noticore.domains.incident.persistence.repository.IncidentInfoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class IncidentQueryService {

    private final IncidentInfoRepository incidentInfoRepository;

    public List<IncidentInfoResponse> getIncidentInfosByCompletion(boolean completion) {
        return incidentInfoRepository.findIncidentInfosByCompletion(completion);
    }
}
