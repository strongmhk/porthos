package com.swyp.noticore.domains.incident.domain.service;

import static com.swyp.noticore.global.response.code.CommonErrorCode.NOT_FOUND;

import com.swyp.noticore.domains.incident.application.dto.response.IncidentDetailResponse;
import com.swyp.noticore.domains.incident.application.dto.response.IncidentInfoResponse;
import com.swyp.noticore.domains.incident.persistence.repository.IncidentInfoRepository;
import com.swyp.noticore.global.exception.ApplicationException;
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

    public IncidentDetailResponse getIncidentDetail(Long incidentId) {
        incidentInfoRepository.findById(incidentId)
            .orElseThrow(() -> ApplicationException.from(NOT_FOUND));

        return incidentInfoRepository.findIncidentDetailById(incidentId);
    }
}
