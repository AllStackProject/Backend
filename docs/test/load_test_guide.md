# 부하 테스트 실행 가이드

## 1. 환경 설정

### 1.1 필수 도구 설치

```bash
# k6 설치 (macOS)
brew install k6

# Redis CLI (macOS — 이미 redis가 설치되어 있다면 포함됨)
brew install redis

# PostgreSQL CLI
brew install postgresql
```

### 1.2 로컬 인프라 확인

```bash
# PostgreSQL 상태 확인
pg_isready -h localhost -p 5432

# Redis 상태 확인
redis-cli ping   # → PONG

# 서버 (SSL)
curl -sk https://localhost:8080
```

### 1.3 테스트 데이터 준비

```bash
# 기존 데이터 초기화 (선택)
psql -h localhost -U seohyun -d fisa -f scripts/reset-test-data.sql

# 대용량 테스트 데이터 삽입
psql -h localhost -U seohyun -d fisa -f scripts/insert-test-data.sql
```

| 테이블          | 데이터 수  | 설명        |
|--------------|--------|-----------|
| Users        | 100    | 테스트 사용자   |
| Organization | 3      | 테스트 조직    |
| Member       | ~150   | 조직당 50명   |
| Video        | 1,500  | 조직당 500개  |
| History      | 7,500+ | 멤버당 약 50개 |
| Scrap        | 1,000  | 스크랩 데이터   |

```bash
# 데이터 확인
psql -h localhost -U seohyun -d fisa -c "
SELECT 'Users' as t, count(*) FROM users
UNION ALL SELECT 'Videos', count(*) FROM video WHERE upload_status = 'COMPLETE'
UNION ALL SELECT 'History', count(*) FROM history;"
```

---

## 2. 프로젝트 구조

```
k6-tests/
├── shared/
│   ├── config.js               # 공통 설정 (BASE_URL, 테스트 데이터)
│   └── auth.js                 # JWT 토큰 인증 헬퍼
├── results/                    # 테스트 결과 저장 (자동 생성)
│   ├── scenario1-indexing/     # 인덱스 시나리오 결과
│   ├── scenario2-cache/        # 캐시 시나리오 결과
│   └── scenario3-pool/         # 커넥션풀 시나리오 결과
├── home-api-test.js            # 홈 조회 API 테스트
├── history-api-test.js         # 시청 기록 조회 API 테스트
├── video-join-api-test.js      # 영상 시청 세션 시작 API 테스트
└── run-scenario.sh             # 시나리오 오케스트레이터 (메인 실행 스크립트)

scripts/
├── add-indexes.sql             # 인덱스 생성 스크립트
├── drop-indexes.sql            # 인덱스 롤백 스크립트
├── insert-test-data.sql        # 대용량 데이터 삽입
└── reset-test-data.sql         # 데이터 초기화

src/main/resources/
├── application-local.yml       # 로컬 환경 설정
└── application-nocache.yml     # 캐시 비활성화 프로필
```

---

## 3. 테스트 실행 — 권장 순서

> 시나리오는 **1 → 2 → 3** 순서로 진행하세요.
> 각 시나리오는 독립적이므로 개별 실행도 가능합니다.

### 한 줄 요약

```bash
cd k6-tests
./run-scenario.sh 1       # 인덱스 (완전 자동)
./run-scenario.sh 2       # 캐시 (서버 재시작 필요)
./run-scenario.sh 3       # 커넥션풀 (서버 재시작 필요)
./run-scenario.sh all     # 전체 순차 실행
```

---

## 4. 시나리오 1: 인덱스 Before/After (완전 자동)

### 목적

인덱스 추가 전후의 쿼리 성능 차이 측정

### 전제 조건

- 서버가 `local` 프로필로 실행 중
- PostgreSQL 접속 가능 (PGPASSWORD=1234)

### 실행

```bash
cd k6-tests
./run-scenario.sh 1
```

### 자동 실행 흐름

```
① drop-indexes.sql 실행 (인덱스 제거)
② Redis 캐시 초기화 (home:*, video:*:info)
③ Before 테스트: 3개 API × k6 실행
④ add-indexes.sql 실행 (인덱스 적용)
⑤ Redis 캐시 초기화
⑥ After 테스트: 3개 API × k6 실행
⑦ drop-indexes.sql 실행 (롤백 — 원래 상태 복원)
```

### 결과 확인

```
results/scenario1-indexing/
├── before-index-home-api-2026-03-03T14-30-00.html          # Before HTML 리포트
├── before-index-home-api-2026-03-03T14-30-00-summary.json  # Before JSON 원시 데이터
├── after-index-home-api-2026-03-03T14-35-00.html           # After HTML 리포트
├── after-index-home-api-2026-03-03T14-35-00-summary.json
├── before-index-history-api-*.html
├── after-index-history-api-*.html
├── before-index-video-join-api-*.html
└── after-index-video-join-api-*.html
```

### 검증

```bash
# 롤백 확인 — 커스텀 인덱스가 0이면 정상
psql -h localhost -U seohyun -d fisa -c \
  "SELECT count(*) FROM pg_indexes WHERE indexname LIKE 'idx_%';"
```

---

## 5. 시나리오 2: 캐시 Before/After (반자동)

### 목적

Redis 캐시 활성화 전후의 응답 시간 차이 측정

### 전제 조건

- 인덱스가 적용된 상태에서 테스트하려면 먼저 `add-indexes.sql` 실행
- 터미널 2개 필요 (서버용 + 테스트 실행용)

### 실행

```bash
cd k6-tests
./run-scenario.sh 2
```

### 반자동 흐름

```
① 스크립트가 "nocache 프로필로 서버 재시작" 안내 표시
   → 서버 터미널에서:
     SPRING_PROFILES_ACTIVE=local,nocache ./gradlew bootRun
   → 서버 시작 후 Enter

② Redis 캐시 초기화
③ Before 테스트 (캐시 꺼진 상태)

④ 스크립트가 "local 프로필로 서버 재시작" 안내 표시
   → 서버 터미널에서:
     SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
   → 서버 시작 후 Enter

⑤ Redis 캐시 초기화
⑥ After 테스트 (캐시 켜진 상태)
```

### 캐시 토글 원리

`application-nocache.yml` 프로필을 추가하면 `app.cache.enabled=false`가 적용됩니다.
`HomeService`와 `VideoService`에서 이 값에 따라 Redis 캐시 읽기/쓰기를 건너뜁니다.

```yaml
# application-nocache.yml
app:
  cache:
    enabled: false
```

### 결과 확인

```
results/scenario2-cache/
├── before-cache-home-api-*.html
├── after-cache-home-api-*.html
├── before-cache-history-api-*.html
├── after-cache-history-api-*.html
├── before-cache-video-join-api-*.html
└── after-cache-video-join-api-*.html
```

### 검증

```bash
# nocache 상태에서 캐시 키가 생성되지 않는지 확인
redis-cli KEYS "home:*"           # → (empty)
redis-cli KEYS "video:*:info"     # → (empty)

# cache 활성 상태에서 After 테스트 후 키 존재 확인
redis-cli KEYS "home:*"           # → home:1:RECENT 등
```

---

## 6. 시나리오 3: Connection Pool 크기 비교 (반자동)

### 목적

HikariCP `maximum-pool-size` 값(10, 50, 100)에 따른 동시 처리량 변화 측정

### 전제 조건

- `application-local.yml`에 `HIKARI_MAX_POOL_SIZE` 환경변수가 파라미터화되어 있어야 함 (이미 설정됨)

### 실행

```bash
cd k6-tests
./run-scenario.sh 3
```

### 반자동 흐름

```
for pool_size in 10 50 100:
  ① 스크립트가 "HIKARI_MAX_POOL_SIZE=${pool_size}로 서버 재시작" 안내
     → 서버 터미널에서:
       HIKARI_MAX_POOL_SIZE=10 SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
     → 서버 시작 후 Enter

  ② Redis 캐시 초기화
  ③ 3개 API 테스트 실행
```

### 결과 확인

```
results/scenario3-pool/
├── pool-10-home-api-*.html
├── pool-10-history-api-*.html
├── pool-10-video-join-api-*.html
├── pool-50-home-api-*.html
├── pool-50-history-api-*.html
├── pool-50-video-join-api-*.html
├── pool-100-home-api-*.html
├── pool-100-history-api-*.html
└── pool-100-video-join-api-*.html
```

### 핵심 비교 지표

| 지표           | pool=10 | pool=50 | pool=100 |
|--------------|---------|---------|----------|
| p95 응답 시간    | ?       | ?       | ?        |
| 503/504 에러 수 | ?       | ?       | ?        |
| 최대 TPS       | ?       | ?       | ?        |

---

## 7. 결과 분석 방법

### 7.1 HTML 리포트 열기

```bash
# macOS에서 리포트 열기
open k6-tests/results/scenario1-indexing/after-index-home-api-*.html

# 또는 파일 탐색기에서 .html 파일 더블클릭
```

HTML 리포트에는 다음이 포함됩니다:

- 요청 수, 에러율, 응답 시간 분포 차트
- p50 / p90 / p95 / p99 백분위 테이블
- 커스텀 메트릭 (home_api_duration 등)

### 7.2 JSON에서 핵심 지표 추출

```bash
# p95, p99, avg 추출
cat results/scenario1-indexing/before-index-home-api-*-summary.json | jq '{
  avg: .metrics.http_req_duration.values.avg,
  p95: .metrics.http_req_duration.values["p(95)"],
  p99: .metrics.http_req_duration.values["p(99)"],
  total_requests: .metrics.http_reqs.values.count,
  error_rate: .metrics.http_req_failed.values.rate
}'
```

### 7.3 Before/After 비교 예시

```bash
echo "=== Before Index ===" && \
cat results/scenario1-indexing/before-index-home-api-*-summary.json | \
  jq '.metrics.http_req_duration.values | {avg, med, "p(95)", "p(99)"}'

echo "=== After Index ===" && \
cat results/scenario1-indexing/after-index-home-api-*-summary.json | \
  jq '.metrics.http_req_duration.values | {avg, med, "p(95)", "p(99)"}'
```

---

## 8. 환경변수 레퍼런스

### run-scenario.sh 환경변수

| 변수           | 기본값                    | 설명             |
|--------------|------------------------|----------------|
| `DB_HOST`    | localhost              | PostgreSQL 호스트 |
| `DB_PORT`    | 5432                   | PostgreSQL 포트  |
| `DB_NAME`    | privideo               | DB 이름          |
| `DB_USER`    | postgres               | DB 사용자         |
| `PGPASSWORD` | 1234                   | DB 비밀번호        |
| `REDIS_HOST` | localhost              | Redis 호스트      |
| `REDIS_PORT` | 6379                   | Redis 포트       |
| `BASE_URL`   | https://localhost:8080 | API 서버 URL     |

### k6 테스트 환경변수

| 변수              | 기본값              | 설명         |
|-----------------|------------------|------------|
| `EMAIL`         | test@example.com | 로그인 이메일    |
| `PASSWORD`      | password123      | 로그인 비밀번호   |
| `ORG_ID`        | 1                | 테스트 조직 ID  |
| `MEMBER_ID`     | 1                | 테스트 멤버 ID  |
| `VIDEO_ID`      | 1                | 테스트 비디오 ID |
| `RESULT_DIR`    | results          | 결과 저장 디렉토리 |
| `RESULT_PREFIX` | (테스트별 자동)        | 결과 파일 접두사  |

### 서버 재시작용 환경변수

| 변수                                     | 용도               |
|----------------------------------------|------------------|
| `SPRING_PROFILES_ACTIVE=local`         | 기본 로컬 실행 (캐시 ON) |
| `SPRING_PROFILES_ACTIVE=local,nocache` | 캐시 비활성화          |
| `HIKARI_MAX_POOL_SIZE=10\|50\|100`     | 커넥션풀 크기 변경       |

---

## 9. 모니터링 (테스트 중 병행)

### PostgreSQL

```sql
-- 현재 활성 연결 수
SELECT count(*)
FROM pg_stat_activity
WHERE state = 'active';

-- 대기 중인 쿼리
SELECT pid, state, wait_event_type, query
FROM pg_stat_activity
WHERE state != 'idle';
```

### Redis

```bash
# 실시간 명령어 모니터링
redis-cli monitor

# 캐시 키 목록 확인
redis-cli KEYS "home:*"
redis-cli KEYS "video:*:info"
```

### HikariCP (Actuator가 활성화된 경우)

```bash
curl -sk https://localhost:8080/actuator/metrics/hikaricp.connections.active
curl -sk https://localhost:8080/actuator/metrics/hikaricp.connections.pending
```

---

## 10. 문제 해결

### SSL 인증서 오류

k6 테스트 시 `--insecure-skip-tls-verify`가 `run-scenario.sh`에 이미 포함되어 있습니다.
개별 실행 시에는 직접 추가하세요:

```bash
k6 run --insecure-skip-tls-verify k6-tests/home-api-test.js
```

### 로그인 실패 (401)

```bash
# 수동으로 로그인 테스트
curl -sk -X POST https://localhost:8080/user/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
```

### PostgreSQL 연결 실패

```bash
# PGPASSWORD 확인
export PGPASSWORD=1234
psql -h localhost -U postgres -d privideo -c "SELECT 1;"

# 사용자 환경에 맞게 DB_USER 등 조정
DB_USER=seohyun DB_NAME=fisa ./run-scenario.sh 1
```

### Connection Pool 고갈 (503/504)

이 에러는 시나리오 3에서 **의도적으로 발생**시키는 것입니다.
pool_size=10일 때 503이 나오고, 50/100에서 줄어드는 것이 정상적인 결과입니다.

### k6-reporter 로드 실패

`handleSummary`에서 사용하는 `benc-uk/k6-reporter`는 URL import 방식입니다.
첫 실행 시 인터넷 연결이 필요하며, 이후 캐시됩니다.
