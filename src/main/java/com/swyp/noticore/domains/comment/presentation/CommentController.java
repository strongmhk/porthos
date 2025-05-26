package com.swyp.noticore.domains.comment.presentation;

import com.swyp.noticore.domains.auth.application.dto.MemberContext;
import com.swyp.noticore.domains.comment.application.dto.request.CommentCreateRequest;
import com.swyp.noticore.domains.comment.application.dto.request.CommentUpdateRequest;
import com.swyp.noticore.domains.comment.application.dto.response.CommentResponse;
import com.swyp.noticore.domains.comment.application.usecase.CommentUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/incidents/{incidentId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentUseCase commentUseCase;

    @PostMapping
    public ResponseEntity<String> createComment (@RequestBody @Valid CommentCreateRequest commentCreateRequest, @PathVariable Long incidentId, @AuthenticationPrincipal MemberContext memberContext) {
        commentUseCase.createComment(commentCreateRequest, incidentId,  memberContext.memberId());
        return ResponseEntity.ok("Comment created");
    }

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> readComments (@PathVariable Long incidentId) {
        List<CommentResponse> commentResponses = commentUseCase.readComments(incidentId);
        return ResponseEntity.ok(Map.of("comments", commentResponses));
    }

    @GetMapping("/{memberId}/list")
    public ResponseEntity<Map<String, Object>> readCommentsByMember (@PathVariable Long incidentId, @PathVariable Long memberId) {
        List<CommentResponse> commentResponses = commentUseCase.readCommentsByMember(incidentId, memberId);
        return ResponseEntity.ok(Map.of("comments", commentResponses));
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<String> updateComment (@RequestBody @Valid CommentUpdateRequest commentUpdateRequest, @PathVariable Long incidentId, @PathVariable Long commentId, @AuthenticationPrincipal MemberContext memberContext) {
        CommentResponse commentResponse = commentUseCase.updateComment(commentUpdateRequest, incidentId, commentId, memberContext.memberId());
        return ResponseEntity.ok("Comment updated: " + commentResponse.comment());
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment (@PathVariable Long incidentId, @PathVariable Long commentId, @AuthenticationPrincipal MemberContext memberContext) {
        commentUseCase.deleteComment(incidentId, commentId, memberContext.memberId());
        return ResponseEntity.ok("Comment deleted");
    }
}
