package com.swyp.noticore.domains.member.persistence.repository;

import com.swyp.noticore.domains.member.persistence.entity.GroupInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.ArrayList;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupInfoRepository extends JpaRepository<GroupInfoEntity, Long> {

    @Query("SELECT g.name FROM GroupInfoEntity g WHERE g.name IN :names")
    List<String> findNameByNameIn(@Param("names") List<String> names);

    List<GroupInfoEntity> findByNameIn(List<String> names);
}
