# 에러 처리 규칙

## ErrorCode 정의

각 도메인별로 `{Domain}ErrorCode` enum을 만들고 `BaseErrorCode`를 구현한다.

```java
@Getter
@AllArgsConstructor
public enum IncidentInfoErrorCode implements BaseErrorCode {

    // 400 Bad Request: {DOMAIN}_{1xx}
    INVALID_GROUP_NAME(HttpStatus.BAD_REQUEST, "INCIDENT_101", "유효하지 않은 그룹명입니다."),

    // 401 Unauthorized: {DOMAIN}_{1xx}
    // 403 Forbidden: {DOMAIN}_{3xx}

    // 404 Not Found: {DOMAIN}_{4xx}
    INCIDENT_NOT_FOUND(HttpStatus.NOT_FOUND, "INCIDENT_401", "존재하지 않는 장애입니다."),

    // 409 Conflict: {DOMAIN}_{9xx}
    // 500 Internal Server Error: {DOMAIN}_{5xx}
    ;

    private final HttpStatus httpStatus;
    private final String customCode;  // "{DOMAIN}_{번호}" 형식
    private final String message;     // 한국어 메시지
}
```

## 커스텀 코드 네이밍 규칙

| 도메인 | 접두사 |
|--------|--------|
| Auth   | AUTH   |
| Incident | INCIDENT |
| Comment | COMMENT |
| Member | MEMBER |
| 공통   | COMMON |

번호 체계:
- `1xx` → 400 Bad Request
- `2xx` → 인증/인가 관련
- `3xx` → 403 Forbidden
- `4xx` → 404 Not Found
- `5xx` → 500 Internal Server Error
- `9xx` → 409 Conflict

## 예외 발생 방법

```java
// 권장 패턴
throw ApplicationException.from(IncidentInfoErrorCode.INCIDENT_NOT_FOUND);

// 또는
throw new ApplicationException(IncidentInfoErrorCode.INCIDENT_NOT_FOUND);
```

## 예외 처리 흐름

```
ApplicationException 발생
    ↓
GlobalExceptionHandler.onThrowException()
    ↓
ApplicationResponse.onFailure(code, message) 반환
    ↓
HTTP 응답 (ErrorCode의 httpStatus 사용)
```

## 공통 에러 코드 (`CommonErrorCode`)

이미 정의된 공통 코드 재활용:
- `INTERNAL_SERVER_ERROR` — 예상치 못한 서버 오류
- `BAD_REQUEST` — 잘못된 요청
- `METHOD_ARGUMENT_NOT_VALID` — 요청 파라미터 검증 실패

## ConstraintViolationException 처리

`@Valid` 또는 `@Validated`로 검증 실패 시 `GlobalExceptionHandler`가 자동 처리.
도메인 서비스에서 별도 try-catch 불필요.

## 외부 API 오류 처리

Slack, SMS, OnCall 등 외부 서비스 호출 시:
- `try-catch`로 감싸고 `log.error()`로 기록
- 외부 서비스 실패가 메인 플로우를 중단시키지 않도록 처리

```java
try {
    slackService.sendSlackAlert(payload, url);
} catch (Exception e) {
    log.error("Failed to send Slack alert to {}: {}", url, e.getMessage());
}
```
