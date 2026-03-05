# 시나리오 2: Redis 캐시 최적화 부하 테스트 결과

> 테스트 일시: 2026-03-05 11:01 ~ 11:19 (KST)
> 테스트 환경: macOS (로컬), Spring Boot + PostgreSQL + Redis

---

## 1. 테스트 조건

| 항목 | 값 |
|------|---|
| 테스트 도구 | k6 v0.55.0 |
| DB 인덱스 | 20개 적용 상태 (인덱스 변수 통제) |
| Before 조건 | `application-nocache.yml` 적용 (`app.cache.enabled=false`) |
| After 조건 | 기본 `local` 프로필 (`app.cache.enabled=true`, 기본값) |
| Redis 캐시 | 각 테스트 전 `FLUSHALL`로 초기화 (Cold Start) |

### 캐시 구현 현황

| 캐시 대상 | 키 패턴 | TTL | 캐싱 데이터 |
|----------|--------|-----|-----------|
| 홈 비디오 목록 | `home:{orgId}:{filter}` | 5분 | 비디오 목록 (id, title, thumbnail, creator 등) |
| 비디오 상세 정보 | `video:{videoId}:info` | 10분 | 비디오 메타데이터 (title, description, watchCnt 등) |
| 시청 세션 | `watch:{sessionId}` | 2시간 | 세션 멤버 ID (중복 시청 방지용) |

> **캐시 미적용 항목**: History(시청기록) API는 Redis 캐시 레이어가 없음. 매 요청마다 DB 직접 조회.

### 부하 설정

| API | 최대 VU | 테스트 시간 |
|-----|---------|-----------|
| Home API | 100명 | 1분 50초 |
| History API | 100명 | 1분 50초 |
| Video Join API | 150명 | 3분 20초 |

---

## 2. 테스트 결과 비교

### Home API (`GET /{orgId}/home?filter=RECENT|POPULAR|RECOMMEND`)

| 지표 | Before (캐시 OFF) | After (캐시 ON) | 개선율 |
|------|-------------------|----------------|--------|
| **평균 응답시간** | 110.6ms | 28.1ms | **74.6% 감소** |
| **중앙값 (p50)** | 21.1ms | 19.6ms | 7.1% 감소 |
| **p90** | 222.5ms | 44.5ms | 80.0% 감소 |
| **p95** | 613.9ms | 64.1ms | **89.6% 감소** |
| **최대 응답시간** | 3,287ms | 422ms | 87.2% 감소 |
| 처리량 (RPS) | 27.8/s | 29.5/s | 6.1% 증가 |
| 에러율 | 0.00% | 0.00% | - |

### History API (`GET /{orgId}/myactivity/video`)

| 지표 | Before (캐시 OFF) | After (캐시 ON) | 개선율 |
|------|-------------------|----------------|--------|
| **평균 응답시간** | 9.5ms | 9.2ms | 3.2% 감소 |
| **중앙값 (p50)** | 7.1ms | 7.1ms | 변화 없음 |
| **p90** | 17.8ms | 16.7ms | 6.2% 감소 |
| **p95** | 22.6ms | 21.5ms | **4.9% 감소** |
| **최대 응답시간** | 109.0ms | 100.9ms | 7.4% 감소 |
| 처리량 (RPS) | 30.1/s | 30.2/s | - |
| 에러율 | 0.00% | 0.00% | - |

### Video Join API (`POST /{orgId}/video/{videoId}/join`)

| 지표 | Before (캐시 OFF) | After (캐시 ON) | 개선율 |
|------|-------------------|----------------|--------|
| **평균 응답시간** | 6.6ms | 6.7ms | 변화 없음 |
| **중앙값 (p50)** | 4.3ms | 4.2ms | 변화 없음 |
| **p90** | 13.5ms | 13.9ms | 변화 없음 |
| **p95** | 18.3ms | 20.6ms | 변화 없음 |
| **최대 응답시간** | 179.4ms | 127.8ms | 28.8% 감소 |
| 처리량 (RPS) | 26.3/s | 26.1/s | - |
| 에러율 | 99.94%* | 99.94%* | - |

> *Video Join API의 에러율은 단일 사용자 반복 요청에 의한 409 Conflict. 비즈니스 로직상 정상.

---

## 3. 분석: 캐시 효과가 API별로 다른 이유

### 3-1. Home API — 캐시 효과 극대화 (p95: 614ms → 64ms)

Home API는 캐시의 효과가 가장 극적으로 나타난 API이다.

**캐시 동작 흐름:**

```
[요청] GET /1/home?filter=RECENT
  ↓
[1] Redis에서 home:1:RECENT 키 조회
  ├── Cache HIT  → Redis 데이터로 즉시 응답 (DB 접근 없음)
  └── Cache MISS → DB 쿼리 실행 → 결과를 Redis에 저장 (TTL: 5분) → 응답
```

**캐시가 효과적인 이유:**

1. **높은 Cache Hit Rate**: 동일한 `orgId + filter` 조합은 3가지(RECENT, POPULAR, RECOMMEND)뿐. VU 100명이 3개 키에 집중하므로 첫 3회 요청 이후 **거의 100% Cache Hit**
2. **비싼 DB 쿼리 회피**: Home API의 `findHomeVideos()`는 video + video_member_group_mapping + member_group_mapping + scrap 4개 테이블 JOIN 쿼리. 캐시 히트 시 이 전체 쿼리 생략
3. **동시성 부하 흡수**: 캐시 OFF 상태에서 VU 100명이 동시에 같은 쿼리를 실행하면 **DB 커넥션 경합** 발생 → p95=614ms, max=3,287ms. 캐시 ON 시 대부분 Redis에서 서빙하므로 DB 부하 격리

**중앙값(p50)이 비슷한 이유 (21.1ms vs 19.6ms):**
- 동시 사용자 수가 적은 구간(Ramp-up 초반, Ramp-down 후반)에서는 캐시 OFF여도 DB 쿼리가 빠름 (인덱스 적용 상태)
- **캐시의 핵심 가치는 평균 속도가 아닌 "고부하 시 Tail Latency 억제"**

### 3-2. History API — 캐시 효과 없음 (p95: 22.6ms → 21.5ms)

| 구분 | 설명 |
|------|-----|
| 캐시 레이어 | **없음** (Redis 캐시 미구현) |
| 쿼리 경로 | 항상 DB 직접 조회: `historyRepository.findByMemberId()` |
| Before/After 차이 | 오차 범위 내 (4.9% 차이는 통계적으로 유의미하지 않음) |

History API의 `findByMemberId()`는 Redis 캐시가 구현되어 있지 않다. `cacheEnabled` 플래그와 무관하게 **매 요청마다 DB를 직접 조회**한다. Before/After 간 미세한 차이(~1ms)는 시스템 부하 변동에 의한 오차이다.

> **시사점**: 인덱스(`idx_history_member_last_watched`)가 적용된 상태에서 History API는 이미 p95=22ms로 충분히 빠르기 때문에, 캐시 추가의 우선순위는 낮음.

### 3-3. Video Join API — 캐시 효과 없음 (p95: 18.3ms → 20.6ms)

Video Join API에는 2종류의 Redis 사용이 존재하지만, 테스트 조건에서 효과가 발현되지 않았다.

| Redis 용도 | 키 패턴 | Before(nocache)에서 동작 | 효과 |
|-----------|--------|------------------------|------|
| 시청 세션 관리 | `watch:{sessionId}` | **동작함** (cacheEnabled와 무관) | Before/After 동일 |
| 비디오 상세 캐시 | `video:{videoId}:info` | 동작 안함 | 첫 1회만 해당, 이후 409 |

**캐시 효과가 없는 이유:**

1. **시청 세션(`watch:*`)은 캐시 토글과 무관**: `createWatchSession()`과 `existsWatchSession()`은 `cacheEnabled` 조건 없이 항상 실행. Before/After 모두 동일하게 Redis 세션 확인
2. **99.94%가 409 응답**: 첫 1회 요청만 비디오 정보 캐시를 사용, 이후 모든 요청은 세션 존재 확인 후 즉시 409 반환. 비디오 정보 캐시 로직에 도달하지 않음

---

## 4. 캐시 적용 범위와 효과 매트릭스

| API | Redis 캐시 | 캐시 대상 | TTL | 효과 |
|-----|-----------|----------|-----|------|
| **Home API** | `home:{orgId}:{filter}` | 비디오 목록 (4테이블 JOIN 결과) | 5분 | **p95 89.6% 감소** |
| **History API** | 없음 | - | - | 효과 없음 |
| **Video Join API** | `video:{videoId}:info` | 비디오 메타데이터 | 10분 | 효과 미미 (409로 미도달) |
| (공통) | `watch:{sessionId}` | 시청 세션 | 2시간 | Before/After 동일 동작 |

---

## 5. 핵심 개선 요약

```
Home API     p95: 614ms → 64ms  (89.6% 감소, ~9.6배 개선)
History API  p95: 22.6ms → 21.5ms (변화 없음, 캐시 미적용)
Video Join   p95: 18.3ms → 20.6ms (변화 없음, 409로 캐시 미도달)
```

---

## 6. 캐시의 역할: "평균 속도 개선"이 아닌 "고부하 안정성 확보"

| 지표 | Before (캐시 OFF) | After (캐시 ON) | 해석 |
|------|-------------------|----------------|------|
| Home p50 (중앙값) | 21.1ms | 19.6ms | **거의 동일** |
| Home p95 | 613.9ms | 64.1ms | **9.6배 차이** |
| Home max | 3,287ms | 422ms | **7.8배 차이** |

중앙값은 거의 동일하지만, p95와 max에서 극적인 차이가 발생한다. 이는 다음을 의미한다:

- **저부하 구간**: 인덱스만으로 충분히 빠름 (캐시 유무 무관)
- **고부하 구간 (VU 80~100)**: 캐시 OFF 시 DB 커넥션 경합 → Tail Latency 급등 (p95=614ms, max=3.3s)
- **캐시의 핵심 가치**: DB 커넥션 풀을 보호하여 **고부하 시에도 일관된 응답시간 보장**

---

## 7. 결론

- Redis 캐시는 **다중 테이블 JOIN을 수행하는 Home API에서만 유의미한 개선** (p95 기준 9.6배)
- 캐시의 핵심 가치는 평균 응답시간 단축이 아닌 **고부하 시 Tail Latency 억제와 DB 부하 격리**
- History API는 캐시 레이어가 없어 개선 효과 없음 → 필요 시 별도 캐시 레이어 추가 고려
- 동일 요청이 반복되는 API(Home API)일수록 캐시 효과가 극대화됨 (3개 filter 조합 → 높은 Hit Rate)
