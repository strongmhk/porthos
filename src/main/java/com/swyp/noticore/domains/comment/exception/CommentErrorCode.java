package com.swyp.noticore.domains.comment.exception;

import com.swyp.noticore.global.response.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CommentErrorCode implements BaseErrorCode {
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT_404", "댓글이 존재하지 않습니다."),
    NOT_COMMENT_OWNER(HttpStatus.UNAUTHORIZED, "COMMENT_401", "해당 댓글 권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}
