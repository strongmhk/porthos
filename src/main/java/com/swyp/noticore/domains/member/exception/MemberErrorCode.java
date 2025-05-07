package com.swyp.noticore.domains.member.exception;

import com.swyp.noticore.global.response.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberErrorCode implements BaseErrorCode {

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_401", "회원을 찾을 수 없습니다."),
    PERMISSION_DENIED(HttpStatus.FORBIDDEN, "AUTH_301", "권한이 없습니다."),
    ;


    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}