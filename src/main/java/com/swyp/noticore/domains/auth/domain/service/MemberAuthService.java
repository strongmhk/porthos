package com.swyp.noticore.domains.auth.domain.service;

import static com.swyp.noticore.domains.auth.exception.AuthErrorCode.INVALID_CREDENTIALS;
import static com.swyp.noticore.domains.auth.utils.PasswordEncoderUtil.verifyPassword;
import static com.swyp.noticore.domains.member.exception.MemberErrorCode.MEMBER_NOT_FOUND;
import static com.swyp.noticore.global.response.code.CommonErrorCode.BAD_REQUEST;

import com.swyp.noticore.domains.auth.application.dto.GenerateTokenRequest;
import com.swyp.noticore.domains.auth.application.mapper.AuthMapper;
import com.swyp.noticore.domains.member.persistence.entity.MemberEntity;
import com.swyp.noticore.domains.member.persistence.repository.MemberRepository;
import com.swyp.noticore.global.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberAuthService {

    private final MemberRepository memberRepository;

    public GenerateTokenRequest validate(String reqEmail, String reqPassword) {
        validateRequest(reqEmail, reqPassword);

        MemberEntity memberEntity = memberRepository.findByEmail(reqEmail)
            .orElseThrow(() -> ApplicationException.from(MEMBER_NOT_FOUND));

        validatePassword(reqPassword, memberEntity.getPassword());

        return AuthMapper.mapToGenerateTokenRequest(
            memberEntity.getEmail(),
            memberEntity.getRole(),
            memberEntity.getId()
        );
    }

    private void validateRequest(String reqEmail, String reqPassword) {
        if (reqEmail.isBlank() || reqPassword.isBlank()) {
            throw ApplicationException.from(BAD_REQUEST);
        }
    }

    private void validatePassword(String rawPassword, String encodedPassword) {
        if (!verifyPassword(rawPassword, encodedPassword)) {
            throw ApplicationException.from(INVALID_CREDENTIALS);
        }
    }
}
