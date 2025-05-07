package com.swyp.noticore.domains.incident.persistence.repository;

import com.swyp.noticore.domains.incident.persistence.entity.IncidentInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentInfoRepository extends JpaRepository<IncidentInfoEntity, Long> {
}
