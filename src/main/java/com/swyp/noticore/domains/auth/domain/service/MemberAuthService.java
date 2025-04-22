package com.swyp.noticore.domains.auth.domain.service;

import static com.swyp.noticore.domains.auth.exception.AuthErrorCode.INVALID_CREDENTIALS;
import static com.swyp.noticore.domains.auth.utils.PasswordEncoderUtil.verifyPassword;

import com.swyp.noticore.domains.auth.application.dto.GenerateTokenRequest;
import com.swyp.noticore.domains.auth.application.mapper.AuthMapper;
import com.swyp.noticore.domains.member.domain.service.MemberGetService;
import com.swyp.noticore.domains.member.persistence.entity.MemberEntity;
import com.swyp.noticore.global.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberAuthService {

    private final MemberGetService memberGetService;

    public GenerateTokenRequest validate(String email, String reqPassword) {
        MemberEntity memberEntity = memberGetService.getMemberByEmail(email);

        validatePassword(reqPassword, memberEntity.getPassword());

        return AuthMapper.mapToGenerateTokenRequest(
            memberEntity.getEmail(),
            memberEntity.getRole(),
            memberEntity.getId()
        );
    }

    private void validatePassword(String rawPassword, String encodedPassword) {
        if (!verifyPassword(rawPassword, encodedPassword)) {
            throw ApplicationException.from(INVALID_CREDENTIALS);
        }
    }
}
