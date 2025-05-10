package com.swyp.noticore.domains.incident.persistence.repository;

import com.swyp.noticore.domains.incident.persistence.entity.IncidentInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface IncidentInfoRepository extends JpaRepository<IncidentInfoEntity, Long>, IncidentInfoQueryDslRepository {

    @Query("""
        SELECT i FROM IncidentInfoEntity i
        WHERE SIZE(i.groups) = 0
    """)
    List<IncidentInfoEntity> findAllWithNoGroups();
}
