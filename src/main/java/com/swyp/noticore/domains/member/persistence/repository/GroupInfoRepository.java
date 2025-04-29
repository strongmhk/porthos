package com.swyp.noticore.domains.member.persistence.repository;

import com.swyp.noticore.domains.member.persistence.entity.GroupInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupInfoRepository extends JpaRepository<GroupInfoEntity, Long> {

    boolean existsByName(String name);
}
