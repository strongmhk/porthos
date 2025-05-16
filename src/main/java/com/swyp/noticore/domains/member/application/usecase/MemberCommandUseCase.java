package com.swyp.noticore.domains.member.application.usecase;

import com.swyp.noticore.domains.member.application.dto.request.MemberRequest;
import com.swyp.noticore.domains.member.application.dto.response.MemberInfo;
import com.swyp.noticore.domains.member.domain.service.MemberCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MemberCommandUseCase {

    private final MemberCommandService memberCommandService;

    public void create(MemberRequest request) {
        memberCommandService.insert(request);
    }

    public MemberInfo get(Long memberId) {
        return memberCommandService.findMember(memberId);
    }

    public void update(MemberRequest request, Long memberId) {
        memberCommandService.updateMember(request, memberId);
    }

    public void delete(Long memberId) {
        memberCommandService.deleteMember(memberId);
    }
}
