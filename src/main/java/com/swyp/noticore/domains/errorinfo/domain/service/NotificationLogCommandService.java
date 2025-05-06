package com.swyp.noticore.domains.errorinfo.domain.service;

import com.swyp.noticore.domains.errorinfo.persistence.entity.IncidentInfoEntity;
import com.swyp.noticore.domains.errorinfo.persistence.entity.NotificationLogEntity;
import com.swyp.noticore.domains.errorinfo.persistence.repository.IncidentInfoRepository;
import com.swyp.noticore.domains.errorinfo.persistence.repository.NotificationLogRepository;
import com.swyp.noticore.domains.member.persistence.entity.MemberEntity;
import com.swyp.noticore.domains.member.persistence.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationLogCommandService {

    private final NotificationLogRepository notificationLogRepository;
    private final IncidentInfoRepository incidentInfoRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void saveLog(Long incidentId, Long memberId) {
        IncidentInfoEntity incident = incidentInfoRepository.getReferenceById(incidentId);
        MemberEntity member = memberRepository.getReferenceById(memberId);

        NotificationLogEntity log = NotificationLogEntity.builder()
                .incident(incident)
                .member(member)
                .isVerified(false)
                .build();

        notificationLogRepository.save(log);
    }

    @Transactional
    public void markAsVerified(Long incidentId, Long memberId) {
        NotificationLogEntity log = notificationLogRepository
                .findByIncidentIdAndMemberId(incidentId, memberId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "해당 incident/member 조합의 알림 로그가 존재하지 않습니다. " +
                        "incidentId=" + incidentId + ", memberId=" + memberId
                ));

        log.setVerified(true);
    }
}
