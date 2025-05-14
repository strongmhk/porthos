package com.swyp.noticore.domains.incident.domain.service;

import com.swyp.noticore.domains.incident.persistence.entity.NotificationLogEntity;
import com.swyp.noticore.domains.incident.persistence.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationLogQueryService {

    private final NotificationLogRepository notificationLogRepository;

    public NotificationLogEntity getNotificationLog (Long incidentId, Long memberId) {
        NotificationLogEntity log = notificationLogRepository.findByIncidentIdAndMemberId(incidentId, memberId)
                .orElseThrow(() ->  new IllegalArgumentException(
                        "해당 incident/member 조합의 알림 로그가 존재하지 않습니다. " +
                        "incidentId=" + incidentId + ", memberId=" + memberId
                ));
        return log;
    }
}
