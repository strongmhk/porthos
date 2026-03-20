package com.swyp.noticore.domains.incident.persistence.repository;

import com.swyp.noticore.domains.incident.persistence.entity.NotificationLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationLogRepository extends JpaRepository<NotificationLogEntity, Long> {
    Optional<NotificationLogEntity> findByIncidentIdAndMemberId(Long incidentId, Long memberId);

    @Modifying
    @Query("DELETE FROM NotificationLogEntity n WHERE n.incident.id IN :incidentIds")
    void deleteByIncidentIdIn(@Param("incidentIds") List<Long> incidentIds);
}
