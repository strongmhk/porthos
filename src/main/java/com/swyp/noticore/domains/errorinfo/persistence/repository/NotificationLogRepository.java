package com.swyp.noticore.domains.errorinfo.persistence.repository;

import com.swyp.noticore.domains.errorinfo.persistence.entity.NotificationLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationLogRepository extends JpaRepository<NotificationLogEntity, Long> {
}
