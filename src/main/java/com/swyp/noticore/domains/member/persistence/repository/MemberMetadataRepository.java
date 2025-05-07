package com.swyp.noticore.domains.member.persistence.repository;

import com.swyp.noticore.domains.member.persistence.entity.MemberMetadataEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberMetadataRepository extends JpaRepository<MemberMetadataEntity, Long> {
}
