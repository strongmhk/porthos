# 에러 코드 추가 스킬

기존 `{Domain}ErrorCode` enum에 새 에러 코드를 추가한다.

## 사용법

"{도메인}에 {상황} 에러 코드 추가해줘"

## 작업 순서

1. 해당 도메인의 `exception/{Domain}ErrorCode.java` 파일 읽기
2. 기존 코드 번호 확인하여 다음 번호 결정
3. HTTP 상태에 맞는 번호 범위 선택:
   - `400 Bad Request` → `{DOMAIN}_1xx`
   - `401/403 Auth` → `{DOMAIN}_2xx`
   - `404 Not Found` → `{DOMAIN}_4xx`
   - `409 Conflict` → `{DOMAIN}_9xx`
   - `500 Server Error` → `{DOMAIN}_5xx`
4. 에러 코드 추가 (항상 마지막 항목 위 `;` 위에 삽입)

## 기존 도메인별 사용 중인 코드 접두사

| 도메인 | 접두사 | 파일 경로 |
|--------|--------|-----------|
| Auth | `AUTH` | `domains/auth/exception/AuthErrorCode.java` |
| Incident | `INCIDENT` | `domains/incident/exception/IncidentInfoErrorCode.java` |
| Comment | `COMMENT` | `domains/comment/exception/CommentErrorCode.java` |
| Member | `MEMBER` | `domains/member/exception/MemberErrorCode.java` |

## 추가 예시

```java
// 기존
INCIDENT_NOT_FOUND(HttpStatus.NOT_FOUND, "INCIDENT_401", "존재하지 않는 장애입니다."),
;

// 추가 후
INCIDENT_NOT_FOUND(HttpStatus.NOT_FOUND, "INCIDENT_401", "존재하지 않는 장애입니다."),
INCIDENT_ALREADY_CLOSED(HttpStatus.CONFLICT, "INCIDENT_901", "이미 종료된 장애입니다."),
;
```

## 에러 코드 사용 위치 확인

추가 후 실제 사용할 Service 또는 UseCase에서:
```java
throw ApplicationException.from({Domain}ErrorCode.{NEW_ERROR_CODE});
```
