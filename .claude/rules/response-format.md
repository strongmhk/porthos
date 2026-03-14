# API 응답 형식 규칙

## ApplicationResponse 래퍼

모든 API 응답은 `ApplicationResponse<T>`를 통해 표준화된 형식으로 반환한다.

```json
{
  "isSuccess": true,
  "code": "COMMON_200",
  "message": "요청에 성공했습니다.",
  "result": { ... }
}
```

실패 응답:
```json
{
  "isSuccess": false,
  "code": "AUTH_101",
  "message": "사용자 인증에 실패했습니다.",
  "result": null
}
```

## 사용 방법

```java
// 성공 (데이터 있음)
return ResponseEntity.ok(ApplicationResponse.onSuccess(result));

// 성공 (데이터 없음)
return ResponseEntity.ok(ApplicationResponse.onSuccess());

// 실패 (GlobalExceptionHandler에서 자동 처리)
throw ApplicationException.from(SomeDomainErrorCode.SOMETHING_NOT_FOUND);
```

## 현재 컨트롤러의 응답 패턴

일부 컨트롤러는 `ApplicationResponse` 없이 `ResponseEntity`를 직접 반환하는 경우도 있다.
새로운 엔드포인트는 가능하면 `ApplicationResponse`를 사용한다.

## 리스트 응답 패턴

복수 항목을 반환할 때는 `Map.of("keyName", list)` 형식 사용:

```java
return ResponseEntity.ok(Map.of("incidents", incidentInfos));
return ResponseEntity.ok(Map.of("comments", commentResponses));
```

## HTTP 상태 코드

| 상황 | 상태 코드 |
|------|----------|
| 조회 성공 | 200 OK |
| 생성 성공 | 200 OK (또는 201 Created) |
| 수정 성공 | 200 OK |
| 삭제 성공 | 200 OK |
| 비동기 수락 | 200 OK ("Accepted" 메시지) |
| 인증 실패 | 401 Unauthorized |
| 권한 없음 | 403 Forbidden |
| 리소스 없음 | 404 Not Found |
| 검증 실패 | 400 Bad Request |
| 서버 오류 | 500 Internal Server Error |
