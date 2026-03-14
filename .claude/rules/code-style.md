# 코드 스타일 규칙

## 기술 스택

- **Java 17**, **Spring Boot 3.2.5**
- **Gradle** 빌드 도구
- **Lombok** 어노테이션 적극 활용
- **QueryDSL 5.0.0** (jakarta)

## 네이밍 컨벤션

| 대상 | 규칙 | 예시 |
|------|------|------|
| 클래스 | PascalCase | `IncidentInfoUseCase` |
| 메서드/변수 | camelCase | `getIncidentDetail` |
| 상수 | UPPER_SNAKE_CASE | `AUTH_101` |
| 패키지 | lowercase | `com.swyp.noticore.domains.incident` |
| DB 컬럼/테이블 | snake_case | `incident_info`, `created_at` |

## Lombok 사용 기준

```java
@Getter              // 필드 getter (Setter는 꼭 필요한 경우만)
@RequiredArgsConstructor  // final 필드 생성자 (DI용)
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // Entity용
@SuperBuilder        // Entity 상속 계층에서 사용
@AllArgsConstructor  // DTO, Enum 등
@Slf4j               // 로깅 필요 클래스
```

## DTO 작성

- Record 또는 Lombok 클래스 모두 허용
- 요청 DTO: `{Action}Request` 이름
- 응답 DTO: `{Domain}Response` 이름
- Validation: `jakarta.validation` 어노테이션 사용 (`@NotNull`, `@NotBlank` 등)

```java
// Record 예시
public record CommentCreateRequest(
    @NotBlank String comment
) {}

// Lombok 예시
@Getter
@AllArgsConstructor
public class IncidentInfoResponse {
    private final Long id;
    private final String title;
}
```

## 컨트롤러 작성

- `@RestController` + `@RequestMapping("/api/...")` 사용
- 응답은 `ResponseEntity<T>` 반환
- 인증 필요 엔드포인트: `@AuthenticationPrincipal MemberContext memberContext` 파라미터 추가

```java
@RestController
@RequestMapping("/api/examples")
@RequiredArgsConstructor
public class ExampleController {

    private final ExampleUseCase exampleUseCase;

    @GetMapping("/{id}")
    public ResponseEntity<ExampleResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(exampleUseCase.get(id));
    }
}
```

## 로깅

- `@Slf4j` 사용
- 에러는 `log.error(...)`, 정보성은 `log.info(...)`, 디버그는 `log.debug(...)`
- 외부 API 호출 실패 시 반드시 로깅

## 비동기 처리

- 즉시 응답이 필요한 무거운 작업은 `CompletableFuture.runAsync(...)` 활용
- (예: `/api/incidents/notify` 엔드포인트 패턴 참고)

## 임포트 정렬

- static import와 일반 import 분리
- 와일드카드 import(`.*`) 지양 (단, `lombok`, 간단한 유틸은 허용)
