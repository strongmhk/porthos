package com.swyp.noticore.domains.incident.exception;

import com.swyp.noticore.global.response.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum IncidentInfoErrorCode implements BaseErrorCode {
    INCIDENT_INFO_NOT_FOUND(HttpStatus.NOT_FOUND, "INCIDENT_INFO_404", "장애 정보가 존재하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}
