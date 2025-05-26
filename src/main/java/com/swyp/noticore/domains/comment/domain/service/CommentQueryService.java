package com.swyp.noticore.domains.comment.domain.service;

import com.swyp.noticore.domains.comment.application.dto.response.CommentResponse;
import com.swyp.noticore.domains.comment.exception.CommentErrorCode;
import com.swyp.noticore.domains.comment.persistence.entity.CommentEntity;
import com.swyp.noticore.domains.comment.persistence.repository.CommentQueryDslRepository;
import com.swyp.noticore.global.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentQueryService {

    private final CommentQueryDslRepository commentQueryDslRepository;

    public CommentResponse readComment(Long incidentId, Long commentId) {
        CommentEntity comment = commentQueryDslRepository.findByIncidentInfoEntityIdAndId(incidentId, commentId).orElseThrow(() ->
                new ApplicationException(CommentErrorCode.COMMENT_NOT_FOUND));

        return CommentResponse.from(comment);
    }

    public List<CommentResponse> readComments(Long incidentId) {
        List<CommentEntity> comments = commentQueryDslRepository.findByIncidentInfoEntityId(incidentId);
        if (comments.isEmpty()) {
            throw new ApplicationException(CommentErrorCode.COMMENT_NOT_FOUND);
        }

        List<CommentResponse> commentResponses = comments.stream().map(CommentResponse::from).toList();
        return commentResponses;
    }


    public List<CommentResponse> readCommentsByMember(Long incidentId, Long memberId) {
        List<CommentEntity> comments = commentQueryDslRepository.findAllByIncidentInfoEntityIdAndMemberId(incidentId, memberId);
        if (comments.isEmpty()) {
            throw new ApplicationException(CommentErrorCode.COMMENT_NOT_FOUND);
        }

        List<CommentResponse> commentResponses = comments.stream().map(CommentResponse::from).toList();
        return commentResponses;
    }
}
