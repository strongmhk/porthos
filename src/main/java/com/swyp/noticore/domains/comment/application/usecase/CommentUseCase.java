package com.swyp.noticore.domains.comment.application.usecase;

import com.swyp.noticore.domains.comment.application.dto.request.CommentCreateRequest;
import com.swyp.noticore.domains.comment.application.dto.request.CommentUpdateRequest;
import com.swyp.noticore.domains.comment.application.dto.response.CommentResponse;
import com.swyp.noticore.domains.comment.domain.service.CommentCommandService;
import com.swyp.noticore.domains.comment.domain.service.CommentQueryService;
import com.swyp.noticore.global.annotation.architecture.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@UseCase
@RequiredArgsConstructor
public class CommentUseCase {

    private final CommentCommandService commentCommandService;
    private final CommentQueryService commentQueryService;

    public void createComment(CommentCreateRequest commentCreateRequest, Long memberId, Long incidentId) {
        commentCommandService.createComment(commentCreateRequest, incidentId, memberId);
    }

    public List<CommentResponse> readComments(Long incidentId) {
        return commentQueryService.readComments(incidentId);
    }

    public List<CommentResponse> readCommentsByMember(Long incidentId, Long memberId) {
        return commentQueryService.readCommentsByMember(incidentId, memberId);
    }

    public CommentResponse updateComment(CommentUpdateRequest commentUpdateRequest, Long incidentId, Long commentId, Long memberId) {
        return commentCommandService.updateComment(commentUpdateRequest, incidentId, commentId, memberId);
    }

    public void deleteComment(Long incidentId, Long commentId, Long memberId) {
        commentCommandService.deleteComment(incidentId, commentId, memberId);
    }
}
