package com.swyp.noticore.domains.comment.domain.service;

import com.swyp.noticore.domains.comment.application.dto.request.CommentCreateRequest;
import com.swyp.noticore.domains.comment.application.dto.request.CommentUpdateRequest;
import com.swyp.noticore.domains.comment.application.dto.response.CommentResponse;
import com.swyp.noticore.domains.comment.exception.CommentErrorCode;
import com.swyp.noticore.domains.comment.persistence.entity.CommentEntity;
import com.swyp.noticore.domains.comment.persistence.repository.CommentRepository;
import com.swyp.noticore.domains.incident.exception.IncidentInfoErrorCode;
import com.swyp.noticore.domains.incident.persistence.entity.IncidentInfoEntity;
import com.swyp.noticore.domains.incident.persistence.repository.IncidentInfoRepository;
import com.swyp.noticore.domains.member.exception.MemberErrorCode;
import com.swyp.noticore.domains.member.persistence.entity.MemberEntity;
import com.swyp.noticore.domains.member.persistence.repository.MemberRepository;
import com.swyp.noticore.global.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentCommandService {

    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final IncidentInfoRepository incidentInfoRepository;

    public void createComment(CommentCreateRequest commentCreateRequest, Long incidentId, Long memberId) {
        IncidentInfoEntity incidentInfo = incidentInfoRepository.findById(incidentId).orElseThrow(() ->
                new ApplicationException(IncidentInfoErrorCode.INCIDENT_INFO_NOT_FOUND));

        MemberEntity member = memberRepository.findById(memberId).orElseThrow(() ->
                new ApplicationException(MemberErrorCode.MEMBER_NOT_FOUND));

        CommentEntity comment = CommentEntity.builder()
                .comment(commentCreateRequest.comment())
                .member(member)
                .incidentInfoEntity(incidentInfo)
                .build();

        commentRepository.save(comment);
    }

    public CommentResponse updateComment(CommentUpdateRequest commentUpdateRequest, Long incidentId, Long commentId, Long memberId) {
        incidentInfoRepository.findById(incidentId).orElseThrow(() ->
                new ApplicationException(IncidentInfoErrorCode.INCIDENT_INFO_NOT_FOUND));

        CommentEntity comment = commentRepository.findById(commentId).orElseThrow(() ->
                new ApplicationException(CommentErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getMember().getId().equals(memberId)) {
            throw new ApplicationException(CommentErrorCode.NOT_COMMENT_OWNER);
        }

        comment.setComment(commentUpdateRequest.comment());

        CommentResponse commentResponse = CommentResponse.builder()
                .commentId(commentId)
                .memberId(comment.getMember().getId())
                .incidentInfoId(incidentId)
                .comment(commentUpdateRequest.comment())
                .build();

        return commentResponse;
    }

    public void deleteComment(Long incidentId, Long commentId, Long memberId) {
        incidentInfoRepository.findById(incidentId).orElseThrow(() ->
                new ApplicationException(IncidentInfoErrorCode.INCIDENT_INFO_NOT_FOUND));
        CommentEntity comment = commentRepository.findById(commentId).orElseThrow(() ->
                new ApplicationException(CommentErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getMember().getId().equals(memberId)) {
            throw new ApplicationException(CommentErrorCode.NOT_COMMENT_OWNER);
        }

        commentRepository.deleteById(commentId);
    }
}
