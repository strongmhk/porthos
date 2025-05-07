package com.swyp.noticore.domains.errorinfo.persistence.repository;

import com.swyp.noticore.domains.errorinfo.persistence.entity.IncidentGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentGroupRepository extends JpaRepository<IncidentGroupEntity, Long> {
}
