package com.swyp.noticore.domains.comment.persistence.repository;

import com.swyp.noticore.domains.comment.persistence.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
}
