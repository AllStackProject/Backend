# 부하 테스트 시나리오

## 1. 테스트 배경

### 1.1 프로젝트 개요

Privideo는 조직 내 비디오 학습 플랫폼으로, 다음과 같은 핵심 기능을 제공합니다:

- 조직별 비디오 관리 및 스트리밍
- 멤버별 시청 기록 관리
- 멤버 그룹 기반 접근 권한 제어
- AI 기반 비디오 요약/피드백/퀴즈 생성

### 1.2 성능 이슈 발생 가능성

ERD 분석 결과, 다음과 같은 성능 병목이 예상됩니다:

| 테이블                        | 예상 데이터량      | 병목 원인                 |
|----------------------------|--------------|-----------------------|
| Video                      | 조직당 수백~수천 개  | 다중 조인, 필터링, 정렬        |
| History                    | 사용자당 수십~수백 개 | 시청 기록 증가에 따른 조회 성능 저하 |
| Video_Member_Group_Mapping | 비디오당 다수      | 접근 권한 확인을 위한 서브쿼리     |
| Member_Group_Mapping       | 멤버당 다수       | 그룹 기반 필터링             |

### 1.3 테스트 목적

1. **성능 병목 지점 식별**: 실제 부하 상황에서 응답 시간이 느려지는 API 확인
2. **최적화 효과 검증**: 인덱싱, 캐싱, Connection Pool 최적화의 실제 효과 측정
3. **시스템 한계 파악**: 동시 사용자 수 증가에 따른 시스템 한계점 확인

---

## 2. 테스트 대상 API

### 2.1 API 1: 홈 조회 API

**엔드포인트**: `GET /{orgId}/home?filter={filter}`

**선정 이유**:

- 서비스의 메인 진입점으로 가장 빈번하게 호출되는 API
- Video, VideoMemberGroupMapping, MemberGroupMapping 등 다중 테이블 조인
- 필터(RECENT, POPULAR, RECOMMEND)에 따른 동적 정렬
- 카테고리 조회를 위한 추가 쿼리 발생

**쿼리 복잡도 분석**:

```
VideoRepositoryImpl.findHomeVideos()
├── Video 테이블 조회
├── LEFT JOIN VideoMemberGroupMapping (접근 권한)
├── LEFT JOIN MemberGroupMapping (멤버 그룹)
├── WHERE 조건: organization_id, upload_status, creator.status
├── GROUP BY: video.id, title, thumbnailKey, ...
└── ORDER BY: filter에 따라 동적 (created_at / watch_cnt)

VideoRepositoryImpl.findCategoriesForHomeVideos()
├── Video 테이블 조회
├── JOIN VideoCategoryMapping
├── JOIN Category
├── LEFT JOIN VideoMemberGroupMapping
└── LEFT JOIN MemberGroupMapping
```

**예상 병목**:

- 인덱스 부재 시 Full Table Scan
- 다중 LEFT JOIN으로 인한 카테시안 곱 가능성
- 서브쿼리(EXISTS)로 인한 추가 오버헤드

---

### 2.2 API 2: 시청 기록 조회 API

**엔드포인트**: `GET /{orgId}/history`

**선정 이유**:

- 사용자별 개인화 데이터 조회
- History와 Video 조인 + Scrap 서브쿼리
- `lastWatchedAt` 기준 정렬로 인한 인덱스 필요성

**쿼리 복잡도 분석**:

```
HistoryRepositoryImpl.findByMemberId()
├── History 테이블 조회
├── JOIN Video (비디오 정보)
├── WHERE: member_id, join_status, upload_status
├── EXISTS 서브쿼리: Scrap 테이블 (스크랩 여부)
└── ORDER BY: last_watched_at DESC
```

**예상 병목**:

- 시청 기록이 많은 사용자의 경우 조회 성능 저하
- `last_watched_at` 컬럼 인덱스 부재 시 정렬 비용 증가
- EXISTS 서브쿼리의 반복 실행

---

### 2.3 API 3: 영상 시청 세션 시작 API

**엔드포인트**: `POST /{orgId}/video/{videoId}/join`

**선정 이유**:

- 비디오 시청의 핵심 진입점
- 다수의 DB 조회 작업이 한 트랜잭션에서 발생
- Redis 세션 관리와 DB 조회가 결합

**쿼리 복잡도 분석**:

```
VideoService.prepareJoinVideoSession()
├── Member 조회: findByIdAndOrganizationIdAndStatus()
├── Video 조회: findById()
├── Redis: existsWatchSession() - 세션 중복 확인
├── MemberGroup: isAccessibleToVideo() - 접근 권한 확인 (복잡한 서브쿼리)
├── Scrap 조회: existsByMemberIdAndVideoId()
├── Category 조회: findAllByVideoId()
├── Quiz 조회: findAllByVideoId() (AI 기능 사용 시)
└── History 조회/생성: findByMemberIdAndVideoId()

VideoService.openWatchSession()
├── Video 조회 (중복)
├── History 조회/생성
├── Redis: createWatchSession()
└── LogService: incOrgViewBucket()
```

**예상 병목**:

- 한 요청에서 다수의 DB 조회 발생 (N+1 문제 가능성)
- Connection Pool 고갈 위험 (동시 요청 증가 시)
- Redis와 DB 간 일관성 문제

---

## 3. 테스트 시나리오

### 3.1 시나리오 1: DB 인덱싱 적용 전후 비교

**목적**: 인덱스 추가로 인한 쿼리 성능 개선 효과 측정

**테스트 흐름**:

```
[인덱스 적용 전]
    │
    ├── 홈 조회 API 부하 테스트 (50 VUs, 60초)
    │   └── 결과 저장: before-indexes-home.json
    │
    ├── 시청 기록 조회 API 부하 테스트 (50 VUs, 60초)
    │   └── 결과 저장: before-indexes-history.json
    │
    └── EXPLAIN ANALYZE로 쿼리 실행 계획 저장
    
[인덱스 추가]
    │
    └── scripts/add-indexes.sql 실행
    
[인덱스 적용 후]
    │
    ├── 홈 조회 API 부하 테스트 (동일 조건)
    │   └── 결과 저장: after-indexes-home.json
    │
    ├── 시청 기록 조회 API 부하 테스트 (동일 조건)
    │   └── 결과 저장: after-indexes-history.json
    │
    └── EXPLAIN ANALYZE로 쿼리 실행 계획 비교
```

**추가 대상 인덱스**:

- History: `(member_id, last_watched_at DESC)`, `(member_id, video_id)`
- Video: `(organization_id, upload_status, created_at DESC)`
- VideoMemberGroupMapping: `(video_id)`, `(member_group_id)`
- MemberGroupMapping: `(member_id, member_group_id)`

**측정 지표**:

| 지표        | 설명           | 목표        |
|-----------|--------------|-----------|
| p50 응답 시간 | 중앙값 응답 시간    | 50% 이상 감소 |
| p95 응답 시간 | 95 백분위 응답 시간 | 50% 이상 감소 |
| TPS       | 초당 처리량       | 2배 이상 증가  |
| 에러율       | 실패 요청 비율     | 5% 미만 유지  |

---

### 3.2 시나리오 2: Redis 캐시 적용 시 데이터 정합성 검증

**목적**: Redis 캐시 적용 후 성능 개선 및 데이터 정합성 확인

**캐시 전략**:

| 캐시 키                    | 데이터       | TTL | 무효화 조건       |
|-------------------------|-----------|-----|--------------|
| `home:{orgId}:{filter}` | 비디오 목록    | 5분  | 비디오 생성/수정/삭제 |
| `video:{videoId}:info`  | 비디오 메타데이터 | 10분 | 비디오 수정/삭제    |

**테스트 흐름**:

```
[캐시 적용 전]
    │
    └── 홈 조회 API 부하 테스트
        └── 결과 저장: before-cache-home.json
    
[캐시 적용 후]
    │
    ├── 홈 조회 API 부하 테스트
    │   └── 결과 저장: with-cache-home.json
    │
    └── 데이터 정합성 검증
        ├── 1. 캐시 히트 확인 (Redis 로그)
        ├── 2. 비디오 수정 후 캐시 무효화 확인
        └── 3. 캐시와 DB 데이터 일치 확인
```

**정합성 검증 시나리오**:

1. 홈 조회 → 캐시 저장 확인
2. 비디오 수정 → 캐시 무효화 확인
3. 홈 재조회 → 최신 데이터 반환 확인

**측정 지표**:

| 지표                | 설명            | 목표      |
|-------------------|---------------|---------|
| 캐시 히트율            | 캐시에서 응답한 비율   | 80% 이상  |
| p50 응답 시간 (캐시 히트) | 캐시 히트 시 응답 시간 | 10ms 미만 |
| 데이터 정합성           | 캐시와 DB 일치율    | 100%    |

---

### 3.3 시나리오 3: Connection Pool 설정 최적화

**목적**: 동시 접속자 증가에 따른 Connection Pool 최적 설정 도출

**테스트 흐름**:

```
[기본 설정 테스트]
    │
    ├── HikariCP 기본값: maximum-pool-size=10
    └── 영상 세션 시작 API 부하 테스트 (점진적 부하 증가)
        ├── 10 VUs → 50 VUs → 100 VUs → 150 VUs
        └── 503/504 에러 발생 지점 확인
    
[최적화 설정 테스트]
    │
    ├── HikariCP 최적화: maximum-pool-size=50
    └── 동일 부하 테스트 재실행
        └── 에러 발생 지점 비교

[추가 최적화 테스트]
    │
    ├── HikariCP 추가 최적화: maximum-pool-size=100
    └── 동일 부하 테스트 재실행
        └── 최적 설정 도출
```

**Connection Pool 설정 옵션**:

| 설정                 | 기본값      | 테스트 값 1  | 테스트 값 2  |
|--------------------|----------|----------|----------|
| maximum-pool-size  | 10       | 50       | 100      |
| minimum-idle       | 10       | 10       | 20       |
| connection-timeout | 30000ms  | 30000ms  | 30000ms  |
| idle-timeout       | 600000ms | 600000ms | 300000ms |

**측정 지표**:

| 지표               | 설명                    | 목표         |
|------------------|-----------------------|------------|
| 503 에러 발생 VUs    | Connection Pool 고갈 시점 | 100 VUs 이상 |
| Connection 대기 시간 | Pool에서 연결 획득 대기 시간    | 100ms 미만   |
| 최대 동시 처리량        | 에러 없이 처리 가능한 최대 VUs   | 100 VUs 이상 |

---

## 4. 테스트 결과에 따른 개선 방향

### 4.1 인덱싱 개선

**예상 결과**:

- 응답 시간 50-70% 감소
- Full Table Scan → Index Scan으로 변경

**개선 방향**:

| 결과        | 조치                        |
|-----------|---------------------------|
| 응답 시간 개선됨 | 인덱스 유지, 복합 인덱스 추가 검토      |
| 개선 미미     | 쿼리 최적화, Covering Index 적용 |
| 특정 쿼리만 개선 | 문제 쿼리 식별 후 개별 최적화         |

**추가 최적화 고려사항**:

- Partial Index 적용 (WHERE 조건 포함)
- Covering Index로 추가 테이블 접근 제거
- 쿼리 리팩토링 (서브쿼리 → JOIN 변환)

---

### 4.2 캐시 개선

**예상 결과**:

- 캐시 히트 시 응답 시간 80-90% 감소
- DB 부하 50% 이상 감소

**개선 방향**:

| 결과        | 조치                   |
|-----------|----------------------|
| 캐시 히트율 높음 | TTL 조정, 캐시 범위 확대     |
| 정합성 문제 발생 | 캐시 무효화 로직 강화, TTL 단축 |
| 캐시 미스 빈번  | 캐시 키 전략 재검토, 프리워밍 적용 |

**추가 최적화 고려사항**:

- Write-Through 캐시 패턴 적용
- 캐시 계층화 (L1: 로컬 캐시, L2: Redis)
- 캐시 프리워밍 (서버 시작 시 주요 데이터 캐싱)

---

### 4.3 Connection Pool 개선

**예상 결과**:

- 동시 처리량 2-3배 증가
- 503 에러 발생 지점 상향

**개선 방향**:

| 결과                  | 조치                             |
|---------------------|--------------------------------|
| Pool 크기 증가 효과 있음    | 최적 Pool 크기 설정 적용               |
| Pool 크기 증가해도 개선 안 됨 | 쿼리 최적화, 비동기 처리 도입              |
| DB 연결 한계 도달         | Read Replica 도입, 커넥션 풀링 서비스 도입 |

**추가 최적화 고려사항**:

- PostgreSQL max_connections 설정 확인
- PgBouncer 등 외부 Connection Pooler 도입
- 읽기/쓰기 분리 (Read Replica)

---

## 5. 결론

### 5.1 테스트 우선순위

1. **인덱싱**: 가장 기본적이고 효과적인 최적화
2. **Connection Pool**: 동시 접속자 증가 대응
3. **Redis 캐시**: 읽기 성능 극대화 및 DB 부하 분산

### 5.2 기대 효과

| 최적화             | 기대 효과                                  |
|-----------------|----------------------------------------|
| 인덱싱             | 쿼리 응답 시간 50-70% 감소                     |
| Redis 캐시        | 캐시 히트 시 응답 시간 80-90% 감소                |
| Connection Pool | 동시 처리량 2-3배 증가                         |
| **종합**          | **p95 응답 시간 500ms 미만, 100+ VUs 동시 처리** |

### 5.3 향후 계획

1. 테스트 결과 기반 최적 설정 도출
2. 프로덕션 환경 적용 전 스테이징 환경에서 재검증
3. 모니터링 시스템 구축 (응답 시간, 에러율, DB 성능)
4. 정기적인 성능 테스트 자동화
