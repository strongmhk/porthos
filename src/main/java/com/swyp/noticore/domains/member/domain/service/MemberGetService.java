package com.swyp.noticore.domains.member.domain.service;

import com.swyp.noticore.domains.auth.exception.AuthErrorCode;
import com.swyp.noticore.domains.member.exception.MemberErrorCode;
import com.swyp.noticore.domains.member.persistence.entity.MemberEntity;
import com.swyp.noticore.domains.member.persistence.repository.MemberRepository;
import com.swyp.noticore.global.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberGetService {

    private final MemberRepository memberRepository;

    public MemberEntity loadMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
            .orElseThrow(() -> ApplicationException.from(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    public MemberEntity loadMemberById(Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> ApplicationException.from(AuthErrorCode.INVALID_CREDENTIALS));
    }
}
