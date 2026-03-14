# 새 API 엔드포인트 추가 스킬

기존 도메인에 새로운 API 엔드포인트를 추가한다.

## 사용법

"{도메인}에 {기능} API 추가해줘"

## 작업 순서

1. 기존 Controller 읽어서 패턴 파악
2. Request/Response DTO 생성 (필요한 경우)
3. UseCase에 메서드 추가
4. CommandService 또는 QueryService에 로직 추가
5. Controller에 엔드포인트 추가

## HTTP 메서드 선택 기준

| 작업 | HTTP 메서드 |
|------|------------|
| 단순 조회 | `GET` |
| 리소스 생성 | `POST` |
| 전체 수정 | `PUT` |
| 부분 수정 | `PATCH` |
| 삭제 | `DELETE` |

## Controller 엔드포인트 패턴

```java
// 목록 조회
@GetMapping("/list")
public ResponseEntity<Map<String, Object>> list(@RequestParam boolean someFilter) {
    return ResponseEntity.ok(Map.of("items", useCase.getList(someFilter)));
}

// 단건 조회
@GetMapping("/{id}")
public ResponseEntity<ExampleResponse> get(@PathVariable Long id) {
    return ResponseEntity.ok(useCase.get(id));
}

// 생성
@PostMapping
public ResponseEntity<String> create(
    @RequestBody @Valid ExampleCreateRequest request,
    @AuthenticationPrincipal MemberContext memberContext
) {
    useCase.create(request, memberContext.memberId());
    return ResponseEntity.ok("created");
}

// 수정
@PatchMapping("/{id}")
public ResponseEntity<String> update(
    @PathVariable Long id,
    @RequestBody @Valid ExampleUpdateRequest request,
    @AuthenticationPrincipal MemberContext memberContext
) {
    useCase.update(id, request, memberContext.memberId());
    return ResponseEntity.ok("updated");
}

// 삭제
@DeleteMapping("/{id}")
public ResponseEntity<String> delete(
    @PathVariable Long id,
    @AuthenticationPrincipal MemberContext memberContext
) {
    useCase.delete(id, memberContext.memberId());
    return ResponseEntity.ok("deleted");
}
```

## 인증 필요 여부 확인

`RequestMatcherHolder` 또는 `SecurityConfig`에서 인증 제외 경로를 확인한다.
기본적으로 모든 `/api/**` 경로는 JWT 인증 필요.

## DTO 검증 어노테이션

```java
public record ExampleCreateRequest(
    @NotBlank(message = "내용은 필수입니다.") String content,
    @NotNull(message = "ID는 필수입니다.") Long targetId,
    @Size(max = 255, message = "최대 255자까지 입력 가능합니다.") String description
) {}
```

## QueryDSL 조회 추가 시

`{Domain}QueryDslRepositoryImpl`에 BooleanExpression을 활용한 동적 쿼리 작성:

```java
@Override
public List<ExampleEntity> findByCondition(boolean someFlag) {
    return queryFactory
        .selectFrom(exampleEntity)
        .where(someFlag ? exampleEntity.active.isTrue() : null)
        .orderBy(exampleEntity.createdAt.desc())
        .fetch();
}
```
