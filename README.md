# 📚 프로젝트 개요

Porthos, swyp-9th-team2-backend </br>
Porthos는 장애 관리 및 모니터링을 위한 웹 애플리케이션 Noticore의 백엔드 파트입니다.

# 👥 팀 정보

| **BE** | **BE** | **BE, SRE** |
| --- | --- | --- |
| 김민형 | 김지민 | 박재호 |
| <img src="https://github.com/strongmhk.png" width="100"> | <img src="https://github.com/jinnieusLab.png" width="100"> | <img src="https://github.com/hojun121.png" width="100"> |
| [strongmhk](https://github.com/strongmhk) | [jinnieusLab](https://github.com/jinnieusLab) | [hojun121](https://github.com/hojun121) |

# 🌟 주요 기능

- **장애 관리**: 장애 등록 및 조회
- **장애 실시간 전파**: 장애 등록 시 즉각적인 알림(Email, SMS, Oncall, Slack) 제공
- **상태 추적**: 장애 처리 상태 실시간 모니터링
- **인증 관리**: 안전한 사용자 인증 및 권한 부여

# ⚙️ 기술 스택

| 구분 | 스택 |
| --- | --- |
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.2.5 |
| **Database** | MySQL(AWS RDS), Redis(AWS Elastic Cache) |
| **ORM / Query** | Spring Data JPA, QueryDSL (Jakarta) |
| **Security & Auth** | Spring Security, JWT (JJWT) |
| **API Docs** | SpringDoc OpenAPI (Swagger) |
| **Cloud & Infra** | AWS SDK (S3, SES, SNS, Lambda) |
| **Mail Parsing** | Jakarta Mail, Angus |

# 🗂️ 디렉토리 구조 및 역할

```
.
├─domains                         # 도메인 중심 구조 (도메인별 aggregate)
│  └─[도메인명]                   # 예: user, comment, incident ...
│     ├─application              # 유스케이스 로직 (서비스에 의존)
│     │  ├─dto                   # 데이터 전달 객체
│     │  │  ├─request            # 클라이언트 → 서버 요청용 DTO
│     │  │  └─response           # 서버 → 클라이언트 응답용 DTO
│     │  ├─mapper                # DTO ↔ Entity 변환
│     │  └─usecase               # 유스케이스 단위 비즈니스 로직
│     ├─domain                   # 핵심 비즈니스 도메인 계층
│     │  ├─constant              # 도메인 관련 상수
│     │  └─service               # 순수 도메인 로직 (비즈니스 규칙)
│     ├─exception                # 도메인 전용 커스텀 예외
│     ├─persistence              # 데이터 영속성 계층
│     │  ├─entity                # JPA 엔티티 클래스 (DB 테이블 매핑)
│     │  └─repository            # DB 접근 계층 (JPA, QueryDSL 등)
│     ├─presentation             # API 진입 지점 (Controller)
│     └─utils                    # 도메인 관련 유틸 클래스
│
├─global                         # 전역 공통 모듈
│  ├─exception                   # 공통 예외 처리
│  ├─response                    # 공통 응답 포맷 & 코드
│  │  └─code                     # 응답 코드 정의
│  ├─utils                       # 전역 유틸 클래스
│  └─config                      # 설정 클래스 (JPA, Redis, Security 등)
│
└─infrastructure                 # 외부 시스템 연동 (Redis, API 등)

```

## 1️⃣ `domains` – **도메인 별 계층 구조 (도메인 중심 아키텍처)**

- 각 도메인(`user`, `incident`, `comment` 등)은 독립된 애그리거트로 구성됩니다.

### 📁 `application` – 애플리케이션 계층

- `dto`: 계층 간 데이터 전달 객체 (Request / Response 구분)
- `mapper`: DTO ↔ Entity 변환 책임
- `usecase`: 사용자 시나리오 중심의 유스케이스 비즈니스 로직 처리
    - 도메인 서비스에 의존하여 핵심 로직을 조합하고 응답 변환 수행

### 📁 `domain` – 도메인 계층

- `constant`: 도메인 내 상수 정의
- `service`: 비즈니스 규칙 및 도메인 로직을 담는 핵심 서비스 계층

### 📁 `exception` – 도메인 예외 처리

- 도메인에서 발생하는 커스텀 예외 클래스 정의

### 📁 `persistence` – 영속성 계층

- `entity`: JPA 엔티티 클래스 (DB 테이블과 1:1 매핑)
- `repository`: 데이터 접근 계층 (쿼리 및 커스텀 메서드 포함)

### 📁 `presentation` – 표현 계층

- REST API 컨트롤러 및 요청 처리 담당
- 유스케이스를 호출하여 응답 반환

### 📁 `infra` *(선택적)* – 도메인 내부 외부 연동

- 해당 도메인 내에서만 사용하는 외부 시스템 연동 구현 (예: 외부 API, 메시지 큐 등)

---

## 2️⃣ `global` – **전역 설정 및 공통 처리**

- `exception`: 프로젝트 전반에 사용되는 예외 및 핸들러 정의
- `response`: 공통 응답 포맷 및 응답 코드 관리
- `config`: 설정 클래스 모음 (JPA, Redis, Security, Swagger 등)
- `utils`: 공통 유틸리티 클래스 (Parser, Helper 등)

---

## 3️⃣ `infrastructure` – **외부 연동 모듈**

- 시스템 전역적으로 사용하는 외부 시스템 연동 기능
    - 예: Redis 설정, 외부 API 호출, 외부 메일 서비스 연동 등

---

## 4️⃣ `utils` *(전역)* – **보조 기능 클래스 모음**

- 다양한 도메인에서 활용 가능한 공통 기능 (예: 문자열 파싱, 날짜 처리 등)
