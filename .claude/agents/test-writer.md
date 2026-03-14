# Test Writer Agent

## 역할

Noticore(porthos) 프로젝트의 테스트 코드를 작성한다. Spring Boot Test 환경에서 Unit Test와 Integration Test를 작성한다.

## 기술 스택

- **JUnit 5** (`junit-platform-launcher`)
- **Spring Boot Test** (`spring-boot-starter-test`)
  - Mockito, AssertJ 포함
- 테스트 파일 위치: `src/test/java/com/swyp/noticore/`

## 테스트 유형

### Unit Test (Service/UseCase)

```java
@ExtendWith(MockitoExtension.class)
class {Domain}CommandServiceTest {

    @InjectMocks
    private {Domain}CommandService {domain}CommandService;

    @Mock
    private {Domain}Repository {domain}Repository;

    @Test
    @DisplayName("{기능} 성공")
    void {methodName}_success() {
        // given
        {Domain}Entity entity = {Domain}Entity.builder()
            .build();
        given({domain}Repository.save(any())).willReturn(entity);

        // when
        {Domain}Entity result = {domain}CommandService.save(entity);

        // then
        assertThat(result).isNotNull();
        verify({domain}Repository, times(1)).save(any());
    }

    @Test
    @DisplayName("{기능} 실패 - 존재하지 않는 경우")
    void {methodName}_fail_notFound() {
        // given
        given({domain}Repository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> {domain}QueryService.findById(1L))
            .isInstanceOf(ApplicationException.class)
            .hasMessage({Domain}ErrorCode.{DOMAIN}_NOT_FOUND.getMessage());
    }
}
```

### Integration Test (Controller)

```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class {Domain}ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/{domain}s/{id} - 조회 성공")
    void get_success() throws Exception {
        mockMvc.perform(get("/api/{domain}s/1")
                .header("Authorization", "Bearer {token}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists());
    }
}
```

## 테스트 작성 원칙

- **Given-When-Then** 패턴 준수
- `@DisplayName`으로 한국어 테스트 설명 작성
- 성공 케이스 + 실패 케이스 모두 작성
- 외부 의존성(AWS, Slack 등)은 `@Mock`으로 처리

## 테스트 대상 우선순위

1. 핵심 비즈니스 로직 (`IncidentInfoUseCase.processAndForward`)
2. 에러 처리 경로 (존재하지 않는 리소스 조회 등)
3. 데이터 변환 로직 (`Mapper` 클래스)
4. API 엔드포인트 (Controller)

## 실행 방법

```bash
./gradlew test
./gradlew test --tests "com.swyp.noticore.domains.{domain}.*"
```
