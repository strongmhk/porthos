package com.swyp.noticore.domains.member.persistence.repository;

import com.swyp.noticore.domains.member.persistence.entity.GroupInfoEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupInfoRepository extends JpaRepository<GroupInfoEntity, Long> {

    boolean exitsByName(String name);
}
