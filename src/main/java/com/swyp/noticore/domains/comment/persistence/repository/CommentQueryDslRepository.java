package com.swyp.noticore.domains.comment.persistence.repository;

import com.swyp.noticore.domains.comment.persistence.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentQueryDslRepository extends JpaRepository <CommentEntity, Long> {
    Optional<CommentEntity> findByIncidentInfoEntityIdAndId(Long incidentId, Long id);
    List<CommentEntity> findByIncidentInfoEntityId(Long incidentId);
    Optional<CommentEntity> findByIncidentInfoEntityIdAndMemberId(Long incidentId, Long memberId);
    List<CommentEntity> findAllByIncidentInfoEntityIdAndMemberId(Long incidentId, Long memberId);
}

