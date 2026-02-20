#!/usr/bin/env bash
set -euo pipefail

# ============================================
# 부하 테스트 시나리오 오케스트레이터
# ============================================
# 사용법:
#   cd k6-tests
#   ./run-scenario.sh 1       # 인덱스 시나리오
#   ./run-scenario.sh 2       # 캐시 시나리오
#   ./run-scenario.sh 3       # 커넥션풀 시나리오
#   ./run-scenario.sh all     # 전체 순차 실행
# ============================================

# ── 컬러 정의 ──
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m' # No Color

# ── 설정 ──
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
RESULTS_BASE="$SCRIPT_DIR/results"

# PostgreSQL 접속 정보
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-privideo}"
DB_USER="${DB_USER:-postgres}"
export PGPASSWORD="${PGPASSWORD:-1234}"

# Redis 접속 정보
REDIS_HOST="${REDIS_HOST:-localhost}"
REDIS_PORT="${REDIS_PORT:-6379}"

# API 서버
BASE_URL="${BASE_URL:-https://localhost:8080}"

# k6 테스트 스크립트 목록
TEST_SCRIPTS=(
  "home-api-test.js"
  "history-api-test.js"
  "video-join-api-test.js"
)

# ── 유틸리티 함수 ──

print_header() {
  echo ""
  echo -e "${BOLD}${BLUE}╔══════════════════════════════════════════════════╗${NC}"
  echo -e "${BOLD}${BLUE}║  $1${NC}"
  echo -e "${BOLD}${BLUE}╚══════════════════════════════════════════════════╝${NC}"
  echo ""
}

print_step() {
  echo -e "${CYAN}▶ $1${NC}"
}

print_success() {
  echo -e "${GREEN}✔ $1${NC}"
}

print_warning() {
  echo -e "${YELLOW}⚠ $1${NC}"
}

print_error() {
  echo -e "${RED}✘ $1${NC}"
}

print_separator() {
  echo -e "${BLUE}──────────────────────────────────────────────────${NC}"
}

wait_for_enter() {
  echo ""
  echo -e "${YELLOW}$1${NC}"
  echo -e "${BOLD}Enter를 눌러 계속 진행하세요...${NC}"
  read -r
}

# ── 헬스체크 ──

check_postgres() {
  print_step "PostgreSQL 연결 확인 중..."
  if psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT 1;" > /dev/null 2>&1; then
    print_success "PostgreSQL 연결 성공"
    return 0
  else
    print_error "PostgreSQL 연결 실패 (host=$DB_HOST, port=$DB_PORT, db=$DB_NAME)"
    return 1
  fi
}

check_redis() {
  print_step "Redis 연결 확인 중..."
  if redis-cli -h "$REDIS_HOST" -p "$REDIS_PORT" ping > /dev/null 2>&1; then
    print_success "Redis 연결 성공"
    return 0
  else
    print_error "Redis 연결 실패 (host=$REDIS_HOST, port=$REDIS_PORT)"
    return 1
  fi
}

check_server() {
  print_step "API 서버 연결 확인 중..."
  if curl -sk --max-time 5 "$BASE_URL" > /dev/null 2>&1; then
    print_success "API 서버 연결 성공 ($BASE_URL)"
    return 0
  else
    print_error "API 서버 연결 실패 ($BASE_URL)"
    return 1
  fi
}

check_k6() {
  print_step "k6 설치 확인 중..."
  if command -v k6 > /dev/null 2>&1; then
    print_success "k6 설치 확인 완료 ($(k6 version 2>&1 | head -1))"
    return 0
  else
    print_error "k6가 설치되어 있지 않습니다. brew install k6"
    return 1
  fi
}

check_all() {
  print_header "환경 헬스체크"
  local failed=0
  check_k6 || failed=1
  check_postgres || failed=1
  check_redis || failed=1
  check_server || failed=1
  if [ $failed -ne 0 ]; then
    print_error "헬스체크 실패. 위의 오류를 확인하세요."
    exit 1
  fi
  print_success "모든 헬스체크 통과"
}

# ── Redis 캐시 초기화 (세션 데이터 보존) ──

flush_test_cache() {
  print_step "Redis 테스트 캐시 초기화 중 (home:*, video:*:info)..."
  local home_keys video_keys
  home_keys=$(redis-cli -h "$REDIS_HOST" -p "$REDIS_PORT" KEYS "home:*" 2>/dev/null || true)
  video_keys=$(redis-cli -h "$REDIS_HOST" -p "$REDIS_PORT" KEYS "video:*:info" 2>/dev/null || true)

  local count=0
  if [ -n "$home_keys" ]; then
    count=$((count + $(echo "$home_keys" | wc -l)))
    echo "$home_keys" | xargs -r redis-cli -h "$REDIS_HOST" -p "$REDIS_PORT" DEL > /dev/null 2>&1
  fi
  if [ -n "$video_keys" ]; then
    count=$((count + $(echo "$video_keys" | wc -l)))
    echo "$video_keys" | xargs -r redis-cli -h "$REDIS_HOST" -p "$REDIS_PORT" DEL > /dev/null 2>&1
  fi
  print_success "Redis 캐시 ${count}개 키 삭제 완료 (세션 데이터 보존)"
}

# ── k6 테스트 실행 ──

run_k6_tests() {
  local result_dir="$1"
  local result_prefix="$2"
  local label="$3"

  mkdir -p "$result_dir"

  for script in "${TEST_SCRIPTS[@]}"; do
    local test_name="${script%.js}"
    local prefix="${result_prefix}-${test_name%-test}"
    print_step "[$label] $test_name 실행 중..."
    k6 run \
      --insecure-skip-tls-verify \
      -e RESULT_DIR="$result_dir" \
      -e RESULT_PREFIX="$prefix" \
      -e BASE_URL="$BASE_URL" \
      "$SCRIPT_DIR/$script" || true
    print_success "[$label] $test_name 완료"
    print_separator
  done
}

# ── SQL 실행 ──

run_sql() {
  local sql_file="$1"
  local label="$2"
  print_step "$label: $sql_file 실행 중..."
  psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -f "$sql_file" > /dev/null 2>&1
  print_success "$label 완료"
}

# ── 인덱스 개수 확인 ──

count_custom_indexes() {
  local count
  count=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c \
    "SELECT count(*) FROM pg_indexes WHERE indexname LIKE 'idx_%';" 2>/dev/null | tr -d ' ')
  echo "$count"
}

# ============================================
# 시나리오 1: 인덱스 Before/After
# ============================================

run_scenario_1() {
  local result_dir="$RESULTS_BASE/scenario1-indexing"
  print_header "시나리오 1: 인덱스 Before/After 테스트"

  # ── Phase 1: Before (인덱스 없이) ──
  print_step "Phase 1: 인덱스 제거 (Before 상태 준비)"
  run_sql "$PROJECT_ROOT/scripts/drop-indexes.sql" "인덱스 삭제"
  local idx_count
  idx_count=$(count_custom_indexes)
  print_success "현재 커스텀 인덱스 수: ${idx_count}"

  flush_test_cache

  print_separator
  echo -e "${BOLD}${YELLOW}[Before] 인덱스 없이 테스트 실행${NC}"
  run_k6_tests "$result_dir" "before-index" "Before-Index"

  # ── Phase 2: After (인덱스 적용) ──
  print_step "Phase 2: 인덱스 적용 (After 상태 준비)"
  run_sql "$PROJECT_ROOT/scripts/add-indexes.sql" "인덱스 생성"
  idx_count=$(count_custom_indexes)
  print_success "현재 커스텀 인덱스 수: ${idx_count}"

  flush_test_cache

  print_separator
  echo -e "${BOLD}${GREEN}[After] 인덱스 적용 후 테스트 실행${NC}"
  run_k6_tests "$result_dir" "after-index" "After-Index"

  # ── 롤백: 인덱스 제거 ──
  print_step "롤백: 인덱스 삭제하여 원래 상태 복원"
  run_sql "$PROJECT_ROOT/scripts/drop-indexes.sql" "인덱스 롤백"
  idx_count=$(count_custom_indexes)
  print_success "롤백 완료. 현재 커스텀 인덱스 수: ${idx_count}"

  print_header "시나리오 1 완료"
  echo -e "결과 디렉토리: ${CYAN}${result_dir}${NC}"
}

# ============================================
# 시나리오 2: 캐시 Before/After
# ============================================

run_scenario_2() {
  local result_dir="$RESULTS_BASE/scenario2-cache"
  print_header "시나리오 2: 캐시 Before/After 테스트"

  # ── Phase 1: Before (캐시 비활성화) ──
  print_warning "캐시 비활성화 상태에서 테스트합니다."
  echo -e "${BOLD}서버를 nocache 프로필로 재시작하세요:${NC}"
  echo -e "  ${CYAN}SPRING_PROFILES_ACTIVE=local,nocache ./gradlew bootRun${NC}"
  wait_for_enter "서버가 nocache 프로필로 시작되면 Enter를 누르세요."

  check_server

  flush_test_cache

  print_separator
  echo -e "${BOLD}${YELLOW}[Before] 캐시 비활성화 상태에서 테스트 실행${NC}"
  run_k6_tests "$result_dir" "before-cache" "Before-Cache"

  # ── Phase 2: After (캐시 활성화) ──
  print_warning "캐시 활성화 상태에서 테스트합니다."
  echo -e "${BOLD}서버를 local 프로필로 재시작하세요:${NC}"
  echo -e "  ${CYAN}SPRING_PROFILES_ACTIVE=local ./gradlew bootRun${NC}"
  wait_for_enter "서버가 local 프로필로 시작되면 Enter를 누르세요."

  check_server

  flush_test_cache

  print_separator
  echo -e "${BOLD}${GREEN}[After] 캐시 활성화 상태에서 테스트 실행${NC}"
  run_k6_tests "$result_dir" "after-cache" "After-Cache"

  print_header "시나리오 2 완료"
  echo -e "결과 디렉토리: ${CYAN}${result_dir}${NC}"
}

# ============================================
# 시나리오 3: Connection Pool 크기 비교
# ============================================

run_scenario_3() {
  local result_dir="$RESULTS_BASE/scenario3-pool"
  local pool_sizes=(10 50 100)

  print_header "시나리오 3: Connection Pool 크기 비교 테스트"

  for pool_size in "${pool_sizes[@]}"; do
    print_separator
    print_warning "HikariCP 커넥션 풀 크기: ${pool_size}"
    echo -e "${BOLD}서버를 HIKARI_MAX_POOL_SIZE=${pool_size}로 재시작하세요:${NC}"
    echo -e "  ${CYAN}HIKARI_MAX_POOL_SIZE=${pool_size} SPRING_PROFILES_ACTIVE=local ./gradlew bootRun${NC}"
    wait_for_enter "서버가 pool_size=${pool_size}로 시작되면 Enter를 누르세요."

    check_server

    flush_test_cache

    echo -e "${BOLD}${BLUE}[Pool=${pool_size}] 테스트 실행${NC}"
    run_k6_tests "$result_dir" "pool-${pool_size}" "Pool-${pool_size}"
  done

  print_header "시나리오 3 완료"
  echo -e "결과 디렉토리: ${CYAN}${result_dir}${NC}"
}

# ============================================
# 메인 실행
# ============================================

usage() {
  echo -e "${BOLD}사용법:${NC} $0 {1|2|3|all}"
  echo ""
  echo "  1    시나리오 1: 인덱스 Before/After (완전 자동)"
  echo "  2    시나리오 2: 캐시 Before/After (서버 재시작 필요)"
  echo "  3    시나리오 3: Connection Pool 크기 비교 (서버 재시작 필요)"
  echo "  all  전체 시나리오 순차 실행"
  echo ""
  echo -e "${BOLD}환경변수:${NC}"
  echo "  DB_HOST, DB_PORT, DB_NAME, DB_USER, PGPASSWORD"
  echo "  REDIS_HOST, REDIS_PORT"
  echo "  BASE_URL (기본값: https://localhost:8080)"
}

main() {
  if [ $# -lt 1 ]; then
    usage
    exit 1
  fi

  local scenario="$1"

  print_header "부하 테스트 시나리오 오케스트레이터"
  echo -e "시나리오: ${BOLD}${scenario}${NC}"
  echo -e "시간: $(date '+%Y-%m-%d %H:%M:%S')"
  print_separator

  check_all

  case "$scenario" in
    1)
      run_scenario_1
      ;;
    2)
      run_scenario_2
      ;;
    3)
      run_scenario_3
      ;;
    all)
      run_scenario_1
      print_separator
      run_scenario_2
      print_separator
      run_scenario_3
      ;;
    *)
      print_error "알 수 없는 시나리오: $scenario"
      usage
      exit 1
      ;;
  esac

  print_header "모든 테스트 완료"
  echo -e "결과 디렉토리: ${CYAN}${RESULTS_BASE}${NC}"
  echo -e "HTML 리포트를 브라우저에서 열어 확인하세요."
}

main "$@"
