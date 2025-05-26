package com.swyp.noticore.domains.comment.application.dto.response;

import com.swyp.noticore.domains.comment.persistence.entity.CommentEntity;
import lombok.Builder;

@Builder
public record CommentResponse (
    Long commentId,
    Long memberId,
    Long incidentInfoId,
    String comment
) {
    public static CommentResponse from(CommentEntity comment) {
        return CommentResponse.builder()
                .commentId(comment.getId())
                .memberId(comment.getMember().getId())
                .incidentInfoId(comment.getIncidentInfoEntity().getId())
                .comment(comment.getComment())
                .build();
    }
}
