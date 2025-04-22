package com.swyp.noticore.domains.member.persistence.repository;

import com.swyp.noticore.domains.member.persistence.entity.MemberEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {

    Optional<MemberEntity> findByEmail(String email);
}
