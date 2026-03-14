# 새 도메인 추가 스킬

새 도메인을 프로젝트의 아키텍처 패턴에 맞게 구현한다.

## 사용법

"[도메인명] 도메인 추가해줘" 또는 "add [domain] domain"

## 생성할 파일 목록

`src/main/java/com/swyp/noticore/domains/{domain}/` 하위:

```
├── presentation/
│   └── {Domain}Controller.java
├── application/
│   ├── usecase/
│   │   └── {Domain}UseCase.java
│   ├── dto/
│   │   ├── request/
│   │   │   └── {Domain}CreateRequest.java
│   │   └── response/
│   │       └── {Domain}Response.java
│   └── mapper/
│       └── {Domain}Mapper.java
├── domain/
│   └── service/
│       ├── {Domain}CommandService.java
│       └── {Domain}QueryService.java
├── persistence/
│   ├── entity/
│   │   └── {Domain}Entity.java
│   └── repository/
│       ├── {Domain}Repository.java
│       ├── {Domain}QueryDslRepository.java
│       └── {Domain}QueryDslRepositoryImpl.java
└── exception/
    └── {Domain}ErrorCode.java
```

## 각 파일 템플릿

### Entity
```java
@Entity
@Table(name = "{table_name}")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class {Domain}Entity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 필드 추가
}
```

### ErrorCode
```java
@Getter
@AllArgsConstructor
public enum {Domain}ErrorCode implements BaseErrorCode {

    {DOMAIN}_NOT_FOUND(HttpStatus.NOT_FOUND, "{DOMAIN}_401", "존재하지 않는 {domain}입니다."),
    ;

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}
```

### CommandService
```java
@Service
@Transactional
@RequiredArgsConstructor
public class {Domain}CommandService {

    private final {Domain}Repository {domain}Repository;

    public {Domain}Entity save({Domain}Entity entity) {
        return {domain}Repository.save(entity);
    }
}
```

### QueryService
```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class {Domain}QueryService {

    private final {Domain}Repository {domain}Repository;

    public {Domain}Entity findById(Long id) {
        return {domain}Repository.findById(id)
            .orElseThrow(() -> ApplicationException.from({Domain}ErrorCode.{DOMAIN}_NOT_FOUND));
    }
}
```

### UseCase
```java
@Slf4j
@Transactional
@UseCase
@RequiredArgsConstructor
public class {Domain}UseCase {

    private final {Domain}CommandService {domain}CommandService;
    private final {Domain}QueryService {domain}QueryService;
}
```

### Controller
```java
@RestController
@RequestMapping("/api/{domain}s")
@RequiredArgsConstructor
public class {Domain}Controller {

    private final {Domain}UseCase {domain}UseCase;
}
```

## 체크리스트

- [ ] Entity extends BaseTimeEntity
- [ ] ErrorCode 번호 기존 코드와 중복 없음
- [ ] CommandService: `@Transactional`, QueryService: `@Transactional(readOnly = true)`
- [ ] UseCase에 `@UseCase` + `@Transactional` 적용
- [ ] Controller에 새 URL 경로가 기존과 충돌하지 않음
