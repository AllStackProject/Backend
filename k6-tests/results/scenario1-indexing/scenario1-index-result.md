# 시나리오 1: DB 인덱스 최적화 부하 테스트 결과

> 테스트 일시: 2026-03-05 10:10 ~ 10:46 (KST)
> 테스트 환경: macOS (로컬), Spring Boot + PostgreSQL + Redis

---

## 1. 테스트 조건

| 항목 | 값 |
|------|---|
| 테스트 도구 | k6 v0.55.0 |
| Before 조건 | 커스텀 인덱스 0개 (PK/FK 기본 인덱스만 존재) |
| After 조건 | Partial Index 20개 적용 (`add-indexes.sql`) |
| Redis 캐시 | 각 테스트 전 `FLUSHALL`로 초기화 |

### 테스트 데이터 규모

| 테이블 | 전체 행 수 | 테스트 대상(org=1) | 비고 |
|--------|-----------|-------------------|------|
| video | 1,500 | 500 | 3개 조직, 각 500건 |
| history | 7,551 | 51 (member=7501) | 시청 기록 |
| video_category_mapping | 4,500 | 1,500 | 비디오당 3개 카테고리 |
| video_member_group_mapping | 600 | - | 비디오 접근 권한 매핑 |
| member_group_mapping | 302 | 2 | 멤버-그룹 매핑 |
| scrap | 1,510 | 10 | 스크랩 |
| member | 151 | 50 (org=1) | - |

### 부하 설정

| API | 최대 VU | 테스트 시간 | 부하 패턴 |
|-----|---------|-----------|----------|
| Home API | 100명 | 1분 50초 | 0→50→100→100→50→0 단계적 증감 |
| History API | 100명 | 1분 50초 | 동일 |
| Video Join API | 150명 | 3분 20초 | 0→20→50→100→100→150→150→0 |

---

## 2. 테스트 결과 비교

### Home API (`GET /{orgId}/home?filter=RECENT|POPULAR|RECOMMEND`)

| 지표 | Before | After | 개선율 |
|------|--------|-------|--------|
| **평균 응답시간** | 124.2ms | 14.9ms | **88.0% 감소** |
| **중앙값 (p50)** | 40.4ms | 13.9ms | 65.6% 감소 |
| **p90** | 365.8ms | 17.7ms | 95.2% 감소 |
| **p95** | 490.2ms | 21.5ms | **95.6% 감소** |
| **최대 응답시간** | 1,260ms | 165ms | 86.9% 감소 |
| 처리량 (RPS) | 28.1/s | 29.8/s | 6.0% 증가 |
| 에러율 | 0.00% | 0.00% | - |
| 총 요청 수 | 3,180 | 3,339 | - |

### History API (`GET /{orgId}/myactivity/video`)

| 지표 | Before | After | 개선율 |
|------|--------|-------|--------|
| **평균 응답시간** | 62.5ms | 8.2ms | **86.9% 감소** |
| **중앙값 (p50)** | 20.9ms | 7.3ms | 65.1% 감소 |
| **p90** | 142.6ms | 10.9ms | 92.4% 감소 |
| **p95** | 258.8ms | 16.0ms | **93.8% 감소** |
| **최대 응답시간** | 1,286ms | 163ms | 87.4% 감소 |
| 처리량 (RPS) | 29.1/s | 30.0/s | 3.1% 증가 |
| 에러율 | 0.00% | 0.00% | - |
| 총 요청 수 | 3,299 | 3,369 | - |

### Video Join API (`POST /{orgId}/video/{videoId}/join`)

| 지표 | Before | After | 개선율 |
|------|--------|-------|--------|
| **평균 응답시간** | 19.4ms | 8.7ms | **55.5% 감소** |
| **중앙값 (p50)** | 7.0ms | 4.3ms | 38.6% 감소 |
| **p90** | 44.3ms | 11.9ms | 73.2% 감소 |
| **p95** | 80.6ms | 22.0ms | **72.7% 감소** |
| **최대 응답시간** | 848ms | 347ms | 59.1% 감소 |
| 처리량 (RPS) | 26.2/s | 26.1/s | - |
| 에러율 | 99.94%* | 99.94%* | - |
| 총 요청 수 | 5,326 | 5,312 | - |

> *Video Join API의 높은 에러율은 단일 테스트 사용자가 동일 영상에 반복 요청하여 발생하는 **409 Conflict("이미 시청 중")**로, 비즈니스 로직상 정상 동작. 첫 1회만 200, 이후 모두 409 반환. 응답시간 지표로 성능 비교 가능.

---

## 3. API별 쿼리-인덱스 매핑 분석

### 3-1. Home API — 가장 큰 개선 (p95: 490ms → 22ms)

Home API가 가장 극적인 개선을 보인 이유는 **다중 테이블 JOIN + 서브쿼리** 구조에서 인덱스 효과가 복합적으로 작용했기 때문이다.

**실행되는 쿼리 체인:**

```
1. memberRepository.findByIdAndOrganizationIdAndStatus()     → member 테이블
2. videoRepository.findHomeVideos()                           → video + JOIN 4개 테이블
3. videoRepository.findCategoriesForHomeVideos()              → video_category_mapping + category
```

**핵심 병목: `findHomeVideos()` QueryDSL 쿼리**

```sql
SELECT v.*, (scrap 존재 여부 서브쿼리)
FROM video v
LEFT JOIN video_member_group_mapping vmgm ON v.id = vmgm.video_id
LEFT JOIN member_group_mapping mgm ON vmgm.member_group_id = mgm.member_group_id
WHERE v.organization_id = ? AND v.upload_status = 'COMPLETE' AND v.status = 'ACTIVE'
  AND (vmgm이 없거나 mgm.member_id = ?)
ORDER BY v.created_at DESC  -- 또는 v.watch_cnt DESC
```

| 적용 인덱스 | 역할 | 효과 |
|------------|------|------|
| `idx_video_org_status_created` | WHERE + ORDER BY 동시 커버 | Full Table Scan → **Index Scan + 정렬 제거** |
| `idx_video_member_group_mapping_video` | LEFT JOIN 조건 최적화 | Nested Loop Join 시 video_id 기반 즉시 탐색 |
| `idx_member_group_mapping_member` | 멤버 그룹 필터링 | member_id 기반 그룹 조회 최적화 |
| `idx_scrap_member_video` | 스크랩 존재 여부 서브쿼리 | EXISTS 서브쿼리 실행 시간 단축 |
| `idx_member_org_status` | 멤버 조회 최적화 | organization_id + status 복합 조건 커버 |

**분석:** 인덱스 미적용 시 `video` 테이블 1,500행을 Full Scan하면서 매 행마다 `video_member_group_mapping`(600행), `member_group_mapping`(302행), `scrap`(1,510행) 서브쿼리를 실행하므로, 동시 사용자 증가 시 **p95가 490ms까지 치솟는 현상** 발생. 인덱스 적용 후 각 JOIN/서브쿼리가 Index Seek로 전환되어 p95가 22ms로 감소.

### 3-2. History API — 정렬 인덱스 효과 (p95: 259ms → 16ms)

**실행되는 쿼리 체인:**

```
1. memberRepository.existsByIdAndOrganizationIdAndStatus()    → member 테이블
2. historyRepository.findByMemberId()                         → history + video + scrap
```

**핵심 병목: `findByMemberId()` QueryDSL 쿼리**

```sql
SELECT h.*, v.*, (scrap 존재 여부 서브쿼리)
FROM history h
JOIN video v ON h.video_id = v.id
WHERE h.member_id = ? AND h.status = 'ACTIVE'
  AND v.upload_status = 'COMPLETE' AND v.join_status = 'APPROVED'
ORDER BY h.last_watched_at DESC
```

| 적용 인덱스 | 역할 | 효과 |
|------------|------|------|
| `idx_history_member_last_watched` | WHERE member_id + ORDER BY last_watched_at DESC | **Covering Index**: 필터링과 정렬을 인덱스만으로 해결 |
| `idx_scrap_member_video` | 스크랩 존재 여부 서브쿼리 | EXISTS 서브쿼리 최적화 |
| `idx_member_org_status` | 멤버 존재 확인 | 복합 조건 인덱스 커버 |

**분석:** `idx_history_member_last_watched`가 `(member_id, last_watched_at DESC)` 복합 인덱스이므로, PostgreSQL이 **별도 정렬(Sort) 없이** 인덱스 순서대로 결과를 반환. 인덱스 미적용 시 history 7,551행 Full Scan → 필터링 → Sort 과정이 필요했으나, 적용 후 Index Scan으로 직접 정렬된 결과 반환.

### 3-3. Video Join API — 다중 쿼리 최적화 (p95: 81ms → 22ms)

**실행되는 쿼리 체인 (가장 복잡):**

```
1. memberRepository.findByIdAndOrganizationIdAndStatus()      → member
2. videoRepository.findById()                                  → video (PK)
3. memberGroupRepository.isAccessibleToVideo()                → video_member_group_mapping + member_group_mapping
4. scrapRepository.existsByMemberIdAndVideoId()               → scrap
5. categoryRepository.findAllByVideoId()                      → video_category_mapping + category
6. historyRepository.findByMemberIdAndVideoId()               → history
7. Redis 세션 생성/조회                                        → Redis
```

| 적용 인덱스 | 역할 | 효과 |
|------------|------|------|
| `idx_video_member_group_mapping_video` | 비디오 접근 권한 확인 (1차) | video_id 기반 그룹 매핑 조회 |
| `idx_video_member_group_mapping_group` | 비디오 접근 권한 확인 (2차) | member_group_id + video_id 복합 조건 |
| `idx_member_group_mapping_member` | 멤버 그룹 소속 확인 | member_id 기반 그룹 조회 |
| `idx_history_member_video` | 기존 시청 기록 조회 | member_id + video_id 복합 조건 |
| `idx_scrap_member_video` | 스크랩 상태 확인 | EXISTS 쿼리 최적화 |
| `idx_video_category_mapping_video` | 카테고리 목록 조회 | video_id 기반 카테고리 매핑 |
| `idx_member_org_status` | 멤버 조회 | 복합 조건 커버 |

**분석:** Video Join API는 **7개 쿼리를 순차 실행**하는 구조로, 개별 쿼리의 개선 폭은 작지만(각 수~십 ms), 인덱스가 7개 쿼리 전체에 걸쳐 효과를 발휘하여 누적 개선이 발생. 특히 `isAccessibleToVideo()`의 2단계 접근 권한 확인 쿼리에서 `idx_video_member_group_mapping_*` 인덱스의 효과가 큼.

---

## 4. 인덱스별 영향 범위

| 인덱스 | Home API | History API | Video Join API |
|--------|:--------:|:-----------:|:--------------:|
| `idx_video_org_status_created` | **핵심** | - | - |
| `idx_history_member_last_watched` | - | **핵심** | - |
| `idx_history_member_video` | - | - | O |
| `idx_video_member_group_mapping_video` | O | - | **핵심** |
| `idx_video_member_group_mapping_group` | - | - | **핵심** |
| `idx_member_group_mapping_member` | O | - | O |
| `idx_scrap_member_video` | O | O | O |
| `idx_video_category_mapping_video` | O | - | O |
| `idx_member_org_status` | O | O | O |

> **핵심**: 해당 API의 주요 병목 쿼리에 직접 작용하는 인덱스
> **O**: 보조적으로 성능에 기여하는 인덱스

---

## 5. 핵심 개선 요약

```
Home API     p95: 490ms → 22ms  (95.6% 감소, ~22배 개선)
History API  p95: 259ms → 16ms  (93.8% 감소, ~16배 개선)
Video Join   p95:  81ms → 22ms  (72.7% 감소, ~3.7배 개선)
```

---

## 6. Tail Latency 분석

| API | Before max | After max | 감소율 | 원인 분석 |
|-----|-----------|----------|--------|----------|
| Home API | 1,260ms | 165ms | 86.9% | Full Scan + 다중 JOIN 제거 |
| History API | 1,286ms | 163ms | 87.4% | Sort 제거 (인덱스 정렬 활용) |
| Video Join | 848ms | 347ms | 59.1% | 7개 쿼리 누적 지연 감소 |

Before 상태에서 **최대 응답시간이 1초를 초과**하는 것은 동시 사용자 증가 시 PostgreSQL의 Shared Buffer 경합과 Full Table Scan의 I/O 비용이 복합적으로 작용한 결과. 인덱스 적용 후 모든 API의 최대 응답시간이 **347ms 이하**로 안정화.

---

## 7. 적용 인덱스 전체 목록 (20개)

| # | 테이블 | 인덱스명 | 컬럼 | 조건 |
|---|--------|---------|------|------|
| 1 | history | `idx_history_member_last_watched` | member_id, last_watched_at DESC | WHERE status='ACTIVE' |
| 2 | history | `idx_history_member_video` | member_id, video_id | WHERE status='ACTIVE' |
| 3 | history | `idx_history_video_member` | video_id, member_id, last_watched_at DESC | WHERE status='ACTIVE' |
| 4 | history | `idx_history_member_completed` | member_id, is_complete, completed_at | WHERE status='ACTIVE' AND is_complete=true |
| 5 | video | `idx_video_org_status_created` | organization_id, upload_status, created_at DESC | WHERE status='ACTIVE' |
| 6 | video | `idx_video_org_creator_status` | organization_id, member_id, upload_status, created_at DESC | WHERE status='ACTIVE' |
| 7 | video | `idx_video_org_title_status` | organization_id, upload_status, title | WHERE status='ACTIVE' |
| 8 | video | `idx_video_video_key` | video_url | WHERE status='ACTIVE' |
| 9 | video_member_group_mapping | `idx_video_member_group_mapping_video` | video_id, status | WHERE status='ACTIVE' |
| 10 | video_member_group_mapping | `idx_video_member_group_mapping_group` | member_group_id, video_id, status | WHERE status='ACTIVE' |
| 11 | member_group_mapping | `idx_member_group_mapping_member` | member_id, member_group_id, status | WHERE status='ACTIVE' |
| 12 | member_group_mapping | `idx_member_group_mapping_group` | member_group_id, member_id, status | WHERE status='ACTIVE' |
| 13 | video_category_mapping | `idx_video_category_mapping_video` | video_id, category_id, status | WHERE status='ACTIVE' |
| 14 | video_category_mapping | `idx_video_category_mapping_category` | category_id, video_id, status | WHERE status='ACTIVE' |
| 15 | scrap | `idx_scrap_member_video` | member_id, video_id, status | WHERE status='ACTIVE' |
| 16 | member | `idx_member_org_status` | organization_id, id, status, join_status | WHERE status='ACTIVE' |
| 17 | member | `idx_member_user_status` | user_id, status, join_status | WHERE status='ACTIVE' |
| 18 | notice_member_group_mapping | `idx_notice_member_group_mapping_notice` | notice_id, member_group_id | (조건 없음) |
| 19 | comment | `idx_comment_video_status` | video_id, status, created_at DESC | WHERE status='ACTIVE' |
| 20 | comment | `idx_comment_parent` | parent_comment_id, status, created_at | WHERE status='ACTIVE' AND is_child=true |

> 19개 인덱스는 `WHERE status = 'ACTIVE'` Partial Index로 생성하여 **비활성 데이터를 인덱스에서 제외**, 인덱스 크기 최소화 및 INSERT/UPDATE 오버헤드 절감.

---

## 8. 결론

- **Home API**의 다중 JOIN 쿼리에서 인덱스 효과가 가장 극대화 (p95 기준 22배 개선)
- Partial Index(`WHERE status = 'ACTIVE'`)를 활용하여 인덱스 크기를 최소화하면서 쿼리 성능 극대화
- 인덱스 적용만으로 모든 API의 **Tail Latency(최대 응답시간)가 1초 이상에서 350ms 이하로 안정화**
- 처리량(RPS)은 인덱스 유무와 무관하게 유사 → 병목이 DB I/O에서 네트워크 오버헤드로 이동한 것으로 판단
