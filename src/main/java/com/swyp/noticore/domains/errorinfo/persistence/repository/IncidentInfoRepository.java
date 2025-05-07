package com.swyp.noticore.domains.errorinfo.persistence.repository;

import com.swyp.noticore.domains.errorinfo.persistence.entity.IncidentInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentInfoRepository extends JpaRepository<IncidentInfoEntity, Long> {
}
