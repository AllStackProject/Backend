#!/bin/bash

# ============================================
# k6 부하 테스트 실행 스크립트
# ============================================
# 사용법:
#   ./run-test.sh [테스트파일] [옵션]
#
# 예시:
#   ./run-test.sh home          # 홈 API 테스트
#   ./run-test.sh history       # 시청 기록 API 테스트
#   ./run-test.sh video-join    # 영상 세션 시작 API 테스트
#   ./run-test.sh all           # 모든 테스트 순차 실행
# ============================================

# 스크립트 디렉토리로 이동
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# .env 파일 로드 (있는 경우)
if [ -f .env ]; then
    echo "📁 .env 파일 로드 중..."
    export $(grep -v '^#' .env | xargs)
fi

# 기본값 설정
BASE_URL=${BASE_URL:-"http://localhost:8080"}
EMAIL=${EMAIL:-"test@example.com"}
PASSWORD=${PASSWORD:-"password123"}
USER_ID=${USER_ID:-1}
MEMBER_ID=${MEMBER_ID:-1}
ORG_ID=${ORG_ID:-1}
VIDEO_ID=${VIDEO_ID:-1}
VUS=${VUS:-10}
DURATION=${DURATION:-"30s"}

# 결과 디렉토리 생성
mkdir -p results

# 환경 변수 출력
echo "============================================"
echo "🔧 테스트 환경 설정"
echo "============================================"
echo "BASE_URL: $BASE_URL"
echo "EMAIL: $EMAIL"
echo "ORG_ID: $ORG_ID"
echo "VIDEO_ID: $VIDEO_ID"
echo "VUS: $VUS"
echo "DURATION: $DURATION"
echo "============================================"

# 공통 k6 옵션
K6_ENV_OPTS="--env BASE_URL=$BASE_URL \
  --env EMAIL=$EMAIL \
  --env PASSWORD=$PASSWORD \
  --env USER_ID=$USER_ID \
  --env MEMBER_ID=$MEMBER_ID \
  --env ORG_ID=$ORG_ID \
  --env VIDEO_ID=$VIDEO_ID \
  --env VUS=$VUS \
  --env DURATION=$DURATION"

# 테스트 실행 함수
run_test() {
    local test_name=$1
    local test_file=$2
    local output_file="results/${test_name}-$(date +%Y%m%d_%H%M%S).json"
    
    echo ""
    echo "🚀 $test_name 테스트 시작..."
    echo "   출력 파일: $output_file"
    echo ""
    
    k6 run $K6_ENV_OPTS \
        --out json="$output_file" \
        "$test_file"
    
    echo ""
    echo "✅ $test_name 테스트 완료"
    echo ""
}

# 메인 로직
case "${1:-help}" in
    home)
        run_test "home-api" "home-api-test.js"
        ;;
    history)
        run_test "history-api" "history-api-test.js"
        ;;
    video-join)
        run_test "video-join-api" "video-join-api-test.js"
        ;;
    all)
        echo "🔄 모든 테스트 순차 실행..."
        run_test "home-api" "home-api-test.js"
        run_test "history-api" "history-api-test.js"
        run_test "video-join-api" "video-join-api-test.js"
        echo "🎉 모든 테스트 완료!"
        ;;
    help|*)
        echo ""
        echo "사용법: ./run-test.sh [테스트명] [옵션]"
        echo ""
        echo "테스트명:"
        echo "  home        - 홈 조회 API 테스트"
        echo "  history     - 시청 기록 조회 API 테스트"
        echo "  video-join  - 영상 시청 세션 시작 API 테스트"
        echo "  all         - 모든 테스트 순차 실행"
        echo "  help        - 도움말 출력"
        echo ""
        echo "환경 변수 설정:"
        echo "  1. .env.example을 .env로 복사 후 수정"
        echo "  2. 또는 export로 직접 설정"
        echo ""
        echo "예시:"
        echo "  ./run-test.sh home"
        echo "  VUS=50 DURATION=60s ./run-test.sh home"
        echo ""
        ;;
esac
