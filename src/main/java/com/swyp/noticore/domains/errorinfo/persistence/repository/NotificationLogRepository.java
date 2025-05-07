package com.swyp.noticore.domains.errorinfo.persistence.repository;

import com.swyp.noticore.domains.errorinfo.persistence.entity.NotificationLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationLogRepository extends JpaRepository<NotificationLogEntity, Long> {
    Optional<NotificationLogEntity> findByIncidentIdAndMemberId(Long incidentId, Long memberId);
}
