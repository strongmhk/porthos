# Noticore QA 모니터링 & 배포 설정 가이드

## 포트 구성

| 서버 | 포트 | 용도 |
|------|------|------|
| QA 서버 | 8081 | 앱 HTTP 요청 |
| QA 서버 | 8082 | Spring Actuator (Prometheus 스크랩) |
| Prometheus 서버 | 9090 | 메트릭 수집 |
| Grafana 서버 | 3000 | 대시보드 |

---

## 1. QA 서버 최초 셋업

### 1-1. 앱 디렉토리 및 설정 파일 준비

```bash
ssh ubuntu@QA_SERVER_IP

mkdir -p /home/ubuntu/porthos

# 환경 설정 파일 배치 (프로덕션과 다른 DB/Redis 정보 사용)
# application-qa.yml 은 JAR 내부에 포함되므로 아래 파일들만 필요
cat > /home/ubuntu/porthos/mysql.yml << 'EOF'
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/porthos_qa?...
    username: qa_user
    password: qa_password
EOF

# 같은 방식으로 redis.yml, jwt.yml, slack.yml 작성
```

### 1-2. systemd 서비스 등록

```bash
sudo cp /home/ubuntu/porthos/noticore-qa.service /etc/systemd/system/

sudo systemctl daemon-reload
sudo systemctl enable noticore-qa
sudo systemctl start noticore-qa

# 상태 확인
sudo systemctl status noticore-qa
journalctl -u noticore-qa -f
```

### 1-3. 방화벽 설정 (UFW 예시)

```bash
# Jenkins 서버 IP에서만 SSH/SCP 허용 (이미 설정된 경우 스킵)
sudo ufw allow from JENKINS_SERVER_IP to any port 22

# Prometheus 서버 IP에서만 8082 포트 허용
sudo ufw allow from PROMETHEUS_SERVER_IP to any port 8082

# 프론트엔드에서 앱 접근 허용
sudo ufw allow 8081
```

---

## 2. Jenkins 설정

### 2-1. 필요 플러그인 설치

Jenkins 관리 → 플러그인 관리에서 설치:
- **Pipeline** (기본 포함)
- **SSH Agent Plugin**
- **Credentials Binding Plugin**
- **Slack Notification Plugin**
- **Git Plugin**

### 2-2. Credentials 등록

Jenkins 관리 → Credentials → System → Global credentials:

| ID | 종류 | 값 |
|----|------|----|
| `qa-server-host` | Secret Text | QA 서버 IP 또는 hostname |
| `qa-ssh-key` | SSH Username with private key | ubuntu 계정의 SSH 개인키 |

### 2-3. Pipeline Job 생성

1. New Item → Pipeline 선택
2. **Build Triggers**: `GitHub hook trigger for GITScm polling` 체크
   (또는 Multibranch Pipeline 사용 시 자동 감지)
3. **Pipeline**: `Pipeline script from SCM` → Git → Repository URL 설정
4. **Script Path**: `Jenkinsfile`

### 2-4. Slack 연동 (선택)

Jenkins 관리 → System → Slack 섹션:
- Workspace: 슬랙 워크스페이스명
- Credential: 슬랙 Bot Token (`#noticore-deploy` 채널 권한 필요)

---

## 3. Prometheus 설정

기존 Prometheus 서버의 `prometheus.yml` 에 scrape config 추가:

```bash
# prometheus.yml 편집
sudo nano /etc/prometheus/prometheus.yml

# prometheus-scrape-config.yml 내용 붙여넣기 후 QA_SERVER_IP 교체

# 설정 재로드 (무중단)
sudo systemctl reload prometheus
# 또는
curl -X POST http://localhost:9090/-/reload
```

스크랩 확인:
```
http://PROMETHEUS_SERVER:9090/targets
→ noticore-qa job이 UP 상태인지 확인
```

---

## 4. Grafana 대시보드 가져오기

1. Grafana 접속 → **Dashboards** → **Import**
2. `monitoring/grafana-dashboard.json` 파일 업로드
3. **Datasource** 드롭다운에서 Prometheus 선택 후 Import
4. 상단 **instance** 변수에서 QA 서버 인스턴스 선택

### 주요 패널 설명

| 패널 | 임계값 기준 | 조치 |
|------|------------|------|
| Heap 사용률 | 85% → 빨간색 | JVM 힙 증가(-Xmx) 또는 메모리 누수 조사 |
| CPU 사용률 | 80% → 빨간색 | 쿼리 최적화, 인스턴스 스케일업 |
| HTTP 에러율 | 5% → 빨간색 | 로그 확인: `journalctl -u noticore-qa` |
| DB 활성 커넥션 | 15개 → 빨간색 | HikariCP maximum-pool-size 검토 |
| HTTP p99 응답시간 | 1s 초과 | 슬로우 쿼리, N+1 쿼리 조사 |

---

## 5. 정상 동작 확인

```bash
# 앱 헬스 체크
curl http://QA_SERVER_IP:8082/actuator/health

# 메트릭 확인
curl http://QA_SERVER_IP:8082/actuator/prometheus | grep http_server

# 앱 로그
journalctl -u noticore-qa -f --since "10 min ago"
```
