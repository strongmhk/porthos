# 아키텍처 규칙

## 레이어 구조

도메인별로 다음 4개의 레이어를 반드시 준수한다.

```
domains/{domain}/
├── presentation/          # Controller (HTTP 진입점)
├── application/
│   ├── usecase/           # @UseCase 클래스 (비즈니스 오케스트레이션)
│   ├── dto/
│   │   ├── request/       # 입력 DTO
│   │   └── response/      # 출력 DTO
│   └── mapper/            # Entity ↔ DTO 변환
├── domain/
│   └── service/           # 핵심 도메인 로직 (Command/Query 분리)
├── persistence/
│   ├── entity/            # JPA Entity
│   └── repository/        # JPA Repository + QueryDSL
└── exception/             # 도메인별 ErrorCode enum
```

## 의존성 방향

`presentation` → `application(UseCase)` → `domain(Service)` → `persistence`

- 역방향 의존성 금지
- UseCase는 다른 도메인의 domain Service는 직접 호출 가능, 다른 도메인의 UseCase 호출 금지
- Controller는 반드시 UseCase를 통해서만 비즈니스 로직 호출

## UseCase 규칙

- `@UseCase` + `@Transactional` 어노테이션 필수
- `@RequiredArgsConstructor`로 의존성 주입
- 클래스명은 `{Domain}UseCase` 형식

```java
@Slf4j
@Transactional
@UseCase
@RequiredArgsConstructor
public class ExampleUseCase {
    // ...
}
```

## Service 분리 원칙

Command/Query Responsibility Segregation(CQRS) 적용:
- `{Domain}CommandService` — 쓰기 작업 (저장, 수정, 삭제)
- `{Domain}QueryService` — 읽기 작업 (조회)

## Entity 규칙

- 모든 Entity는 `BaseTimeEntity` 상속 (createdAt, updatedAt 자동 관리)
- `@SuperBuilder`, `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 필수
- 테이블명은 snake_case로 `@Table(name = "...")` 명시

```java
@Entity
@Table(name = "example")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExampleEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
```

## Repository 규칙

- 기본 CRUD: `JpaRepository` 상속
- 복잡한 조회: `{Domain}QueryDslRepository` 인터페이스 + `{Domain}QueryDslRepositoryImpl` 구현체로 분리

## 글로벌 패키지 위치

- `global/config/` — 설정 클래스 (Security, JPA, QueryDSL, Redis, Swagger)
- `global/entity/` — 공통 Base Entity
- `global/exception/` — 공통 예외 처리
- `global/response/` — API 응답 래퍼
- `global/annotation/` — 커스텀 어노테이션
- `infrastructure/` — 외부 인프라 연동 (Redis 등)
