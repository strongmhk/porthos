package com.swyp.noticore.domains.errorinfo.domain.service;

import com.swyp.noticore.domains.errorinfo.model.NotificationMethod;
import com.swyp.noticore.domains.errorinfo.model.NotificationStatus;
import com.swyp.noticore.domains.errorinfo.persistence.entity.NotificationLogEntity;
import com.swyp.noticore.domains.errorinfo.persistence.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationLogCommandService {

    private final NotificationLogRepository notificationLogRepository;

    /**
     * notification_log 저장
     *
     * @param incidentId  장애 ID
     * @param memberId    대상 회원 ID
     * @param method      전파 수단 (EMAIL, SMS, ONCALL, SLACK)
     * @param status      성공 여부 (SUCCESS, FAIL)
     * @param sentAt      발송 시각
     * @param retryCount  재시도 횟수 (기본 0)
     */
    public void saveLog(Long incidentId, Long memberId, NotificationMethod method, NotificationStatus status, LocalDateTime sentAt, int retryCount) {
        NotificationLogEntity log = NotificationLogEntity.builder()
            .incidentId(incidentId)
            .memberId(memberId)
            .method(method)
            .status(status)
            .sentAt(sentAt)
            .retryCount(retryCount)
            .isVerified(false)
            .build();

        notificationLogRepository.save(log);
    }
}
