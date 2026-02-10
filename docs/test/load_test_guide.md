# 부하 테스트 실행 가이드

## 1. 환경 설정

### 1.1 k6 설치

```bash
# macOS
brew install k6

# Linux (Debian/Ubuntu)
sudo gpg -k
sudo gpg --no-default-keyring --keyring /usr/share/keyrings/k6-archive-keyring.gpg \
    --keyserver hkp://keyserver.ubuntu.com:80 \
    --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" \
    | sudo tee /etc/apt/sources.list.d/k6.list
sudo apt-get update
sudo apt-get install k6

# Windows
choco install k6
```

### 1.2 로컬 환경 구성

#### PostgreSQL 실행 확인
```bash
# PostgreSQL 상태 확인
pg_isready -h localhost -p 5432

# DB 접속 테스트
psql -h localhost -U seohyun -d fisa -c "SELECT 1"
```

#### Redis 실행 확인
```bash
# Redis 상태 확인
redis-cli ping
# 응답: PONG

# Redis 연결 테스트
redis-cli -h localhost -p 6379 info
```

#### 애플리케이션 설정 확인

`application-local.yml` 주요 설정:
```yaml
# PostgreSQL 설정
spring:
  datasource:
    url: "jdbc:postgresql://localhost:5432/fisa"
    hikari:
      maximum-pool-size: 50    # Connection Pool 크기
      minimum-idle: 10

# Redis 설정
spring:
  data:
    redis:
      host: localhost
      port: 6379

# AWS/Gemini는 더미 값으로 설정 (로컬 테스트용)
```

### 1.3 테스트 데이터 준비

#### 대용량 테스트 데이터 삽입

부하 테스트를 위한 대용량 데이터를 삽입합니다:

```bash
# 1. 기존 데이터 초기화 (선택사항)
psql -h localhost -U seohyun -d fisa -f scripts/reset-test-data.sql

# 2. 테스트 데이터 삽입
psql -h localhost -U seohyun -d fisa -f scripts/insert-test-data.sql
```

**삽입되는 데이터 규모:**

| 테이블                        | 데이터 수    | 설명                |
|-----------------------------|----------|-------------------|
| Users                       | 100      | 테스트 사용자           |
| Organization                | 3        | 테스트 조직            |
| Member                      | ~150     | 조직당 50명           |
| Member_Group                | 15       | 조직당 5개            |
| Video                       | 1,500    | 조직당 500개 (대용량)    |
| Category                    | 75       | 멤버 그룹당 5개         |
| History                     | 7,500+   | 멤버당 약 50개 (대용량)   |
| Scrap                       | 1,000    | 스크랩 데이터           |

**테스트 계정 정보:**

```
Email: test@example.com
Password: password123
```

#### 데이터 확인

```sql
-- 테스트용 사용자 확인
SELECT id, email FROM users WHERE email = 'test@example.com';

-- 테스트용 조직 확인
SELECT id, name FROM organization WHERE status = 'ACTIVE';

-- 테스트용 비디오 확인
SELECT id, title, upload_status 
FROM video 
WHERE organization_id = 1 AND upload_status = 'COMPLETE'
LIMIT 10;

-- 데이터 카운트 확인
SELECT 'Users' as table_name, COUNT(*) as count FROM users
UNION ALL SELECT 'Organizations', COUNT(*) FROM organization
UNION ALL SELECT 'Members', COUNT(*) FROM member
UNION ALL SELECT 'Videos', COUNT(*) FROM video
UNION ALL SELECT 'Histories', COUNT(*) FROM history
UNION ALL SELECT 'Scraps', COUNT(*) FROM scrap;
```

---

## 2. 테스트 스크립트 구조

```
k6-tests/
├── shared/
│   ├── config.js      # 공통 설정 (BASE_URL, 테스트 데이터)
│   └── auth.js        # JWT 토큰 인증 헬퍼
├── results/                 # 테스트 결과 저장 디렉토리
├── home-api-test.js         # 홈 조회 API 테스트
├── history-api-test.js      # 시청 기록 조회 API 테스트
├── video-join-api-test.js   # 영상 시청 세션 시작 API 테스트
└── run-test.sh              # 테스트 실행 스크립트
```

---

## 3. 테스트 실행 방법

### 3.1 환경 변수 설정

```bash
# 필수 환경 변수
export BASE_URL="https://localhost:8080"
export EMAIL="test@example.com"
export PASSWORD="password123"
export ORG_ID=1
export MEMBER_ID=1
export VIDEO_ID=1

# 선택 환경 변수
export VUS=10                # 가상 사용자 수 (기본: 10)
export DURATION="30s"        # 테스트 지속 시간 (기본: 30s)
```

### 3.2 홈 조회 API 테스트

```bash
# 기본 실행
k6 run k6-tests/home-api-test.js

# 환경 변수 지정 실행
k6 run \
  --env BASE_URL=https://localhost:8080 \
  --env EMAIL=test@example.com \
  --env PASSWORD=password123 \
  --env ORG_ID=1 \
  k6-tests/home-api-test.js

# 결과 저장
k6 run \
  --out json=k6-tests/results/home-api-results.json \
  k6-tests/home-api-test.js
```

### 3.3 시청 기록 조회 API 테스트

```bash
k6 run \
  --env BASE_URL=https://localhost:8080 \
  --env EMAIL=test@example.com \
  --env PASSWORD=password123 \
  --env ORG_ID=1 \
  --env MEMBER_ID=1 \
  --out json=results/history-api-results.json \
  k6-tests/history-api-test.js
```

### 3.4 영상 시청 세션 시작 API 테스트

```bash
# 기본 실행
k6 run \
  --env BASE_URL=https://localhost:8080 \
  --env EMAIL=test@example.com \
  --env PASSWORD=password123 \
  --env ORG_ID=1 \
  --env VIDEO_ID=1 \
  --out json=results/video-join-api-results.json \
  k6-tests/video-join-api-test.js

# 고부하 테스트 (VUs 수동 지정)
k6 run \
  --vus 100 \
  --duration 60s \
  --out json=results/video-join-high-load.json \
  k6-tests/video-join-api-test.js
```

---

## 4. 시나리오별 테스트 실행

### 4.1 시나리오 1: 인덱스 적용 전후 비교

#### Step 1: 인덱스 적용 전 테스트
```bash
# 결과 디렉토리 생성
mkdir -p results/indexing

# 홈 조회 API 테스트
k6 run \
  --out json=results/indexing/before-home.json \
  k6-tests/home-api-test.js

# 시청 기록 조회 API 테스트
k6 run \
  --out json=results/indexing/before-history.json \
  k6-tests/history-api-test.js
```

#### Step 2: 인덱스 추가
```bash
psql -h localhost -U seohyun -d fisa -f scripts/add-indexes.sql
```

#### Step 3: 인덱스 적용 후 테스트
```bash
# 홈 조회 API 테스트
k6 run \
  --out json=results/indexing/after-home.json \
  k6-tests/home-api-test.js

# 시청 기록 조회 API 테스트
k6 run \
  --out json=results/indexing/after-history.json \
  k6-tests/history-api-test.js
```

#### Step 4: 쿼리 실행 계획 비교
```sql
-- 인덱스 적용 전/후 쿼리 실행 계획 비교
EXPLAIN ANALYZE
SELECT v.id, v.title, v.thumbnail_url, v.created_at, v.watch_cnt
FROM video v
WHERE v.organization_id = 1 
  AND v.upload_status = 'COMPLETE'
ORDER BY v.created_at DESC;
```

---

### 4.2 시나리오 2: Redis 캐시 테스트

#### Step 1: 캐시 비활성화 테스트
```bash
mkdir -p results/cache

# 캐시 비활성화 상태에서 테스트
# (HomeService에서 캐시 로직 주석 처리 필요)
k6 run \
  --out json=results/cache/before-cache.json \
  k6-tests/home-api-test.js
```

#### Step 2: 캐시 활성화 테스트
```bash
# 캐시 활성화 상태에서 테스트
k6 run \
  --out json=results/cache/with-cache.json \
  k6-tests/home-api-test.js
```

#### Step 3: 캐시 히트율 확인
```bash
# Redis CLI로 캐시 확인
redis-cli keys "home:*"
redis-cli keys "video:*"

# 캐시 TTL 확인
redis-cli ttl "home:1:RECENT"
```

---

### 4.3 시나리오 3: Connection Pool 최적화

#### Step 1: 기본 설정 테스트
```bash
mkdir -p results/pool

# 기본 Pool 크기 (10)로 테스트
k6 run \
  --vus 50 \
  --duration 60s \
  --out json=results/pool/default-pool.json \
  k6-tests/video-join-api-test.js
```

#### Step 2: Pool 크기 50으로 테스트
```bash
# application-local.yml 수정 후 서버 재시작
# maximum-pool-size: 50

k6 run \
  --vus 100 \
  --duration 60s \
  --out json=results/pool/pool-50.json \
  k6-tests/video-join-api-test.js
```

#### Step 3: Pool 크기 100으로 테스트
```bash
# application-local.yml 수정 후 서버 재시작
# maximum-pool-size: 100

k6 run \
  --vus 150 \
  --duration 60s \
  --out json=results/pool/pool-100.json \
  k6-tests/video-join-api-test.js
```

---

## 5. 결과 분석

### 5.1 k6 결과 메트릭

| 메트릭 | 설명 |
|--------|------|
| `http_req_duration` | HTTP 요청 지속 시간 |
| `http_req_failed` | 실패한 요청 비율 |
| `http_reqs` | 총 요청 수 |
| `iterations` | 총 반복 횟수 |
| `vus` | 가상 사용자 수 |

### 5.2 결과 파일 분석

```bash
# JSON 결과 파일 확인
cat results/home-api-results.json | jq '.metrics.http_req_duration'

# 주요 지표 추출
cat results/home-api-results.json | jq '{
  avg: .metrics.http_req_duration.values.avg,
  p95: .metrics.http_req_duration.values["p(95)"],
  p99: .metrics.http_req_duration.values["p(99)"]
}'
```

### 5.3 결과 비교

```bash
# 인덱스 적용 전후 비교
echo "=== Before Indexes ===" && \
cat results/indexing/before-home.json | jq '.metrics.http_req_duration.values'

echo "=== After Indexes ===" && \
cat results/indexing/after-home.json | jq '.metrics.http_req_duration.values'
```

---

## 6. 모니터링

### 6.1 PostgreSQL 모니터링

```sql
-- 현재 연결 수 확인
SELECT count(*) FROM pg_stat_activity;

-- 대기 중인 쿼리 확인
SELECT pid, state, query, wait_event_type, wait_event
FROM pg_stat_activity
WHERE state != 'idle';

-- 슬로우 쿼리 확인
SELECT query, calls, mean_time, max_time
FROM pg_stat_statements
ORDER BY mean_time DESC
LIMIT 10;
```

### 6.2 Redis 모니터링

```bash
# 실시간 명령어 모니터링
redis-cli monitor

# 메모리 사용량 확인
redis-cli info memory

# 키 개수 확인
redis-cli dbsize
```

### 6.3 HikariCP 모니터링 (Spring Boot Actuator)

```bash
# Connection Pool 상태 확인 (Actuator 활성화 필요)
curl http://localhost:8080/actuator/metrics/hikaricp.connections.active
curl http://localhost:8080/actuator/metrics/hikaricp.connections.idle
curl http://localhost:8080/actuator/metrics/hikaricp.connections.pending
```

---

## 7. 문제 해결

### 7.1 로그인 실패

```bash
# 원인 확인
curl -X POST https://localhost:8080/user/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}' \
  -k -v

# 해결 방법
# 1. 이메일/비밀번호 확인
# 2. PostgreSQL 연결 확인
# 3. JWT 설정 확인 (application-local.yml)
```

### 7.2 401/403 에러

```bash
# 토큰 유효성 확인
# 1. 토큰 발급 시간 확인 (expired-in 설정)
# 2. 조직 ID와 토큰 내 orgId 일치 확인

# JWT 디코딩 (https://jwt.io 또는)
echo "토큰값" | cut -d'.' -f2 | base64 -d | jq
```

### 7.3 503/504 에러

```bash
# Connection Pool 상태 확인
psql -h localhost -U seohyun -d fisa -c \
  "SELECT count(*) FROM pg_stat_activity WHERE state = 'active';"

# 해결 방법
# 1. HikariCP maximum-pool-size 증가
# 2. PostgreSQL max_connections 확인
# 3. 쿼리 최적화
```

### 7.4 SSL 인증서 오류

```bash
# k6 실행 시 SSL 검증 비활성화
k6 run --insecure-skip-tls-verify k6-tests/home-api-test.js

# 또는 shared/config.js에서 설정
# baseUrl: 'https://localhost:8080'를 사용할 때 --insecure 옵션 필요
```

---

## 8. 로컬 테스트 제한사항

### 8.1 테스트 불가 API

| API | 제한 이유 |
|-----|-----------|
| `POST /{orgId}/video` | S3 업로드 필요 |
| `POST /{orgId}/video/airflow/status` | Airflow 연동 필요 |
| AI 기능 처리 | Gemini AI 호출 필요 |

### 8.2 테스트 가능 API

| API | 비고 |
|-----|------|
| `GET /{orgId}/home` | 홈 조회 |
| `GET /{orgId}/history` | 시청 기록 조회 |
| `POST /{orgId}/video/{videoId}/join` | 영상 세션 시작 (S3 URL은 더미) |
| `GET /{orgId}/video/{videoId}` | 영상 정보 조회 |
| `GET /{orgId}/home/search` | 영상 검색 |
| `GET /{orgId}/home/notice` | 공지사항 조회 |

### 8.3 테스트 데이터 요구사항

로컬 테스트를 위해 다음 데이터가 필요합니다:

1. **사용자 계정**: 로그인 가능한 테스트 계정
2. **조직**: 테스트용 조직 (ACTIVE 상태)
3. **비디오**: 업로드 완료된 비디오 (upload_status = 'COMPLETE')
4. **히스토리**: 시청 기록 데이터 (History 테이블)

```sql
-- 테스트 데이터 확인 쿼리
SELECT 
  'Users' as table_name, count(*) as count FROM users
UNION ALL
SELECT 'Organizations', count(*) FROM organization WHERE status = 'ACTIVE'
UNION ALL
SELECT 'Videos', count(*) FROM video WHERE upload_status = 'COMPLETE'
UNION ALL
SELECT 'History', count(*) FROM history;
```
