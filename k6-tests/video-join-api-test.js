import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';
import { htmlReport } from 'https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js';
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.1.0/index.js';
import { config } from './shared/config.js';
import { login, getAuthHeaders } from './shared/auth.js';

// 커스텀 메트릭
const errorRate = new Rate('errors');
const videoJoinApiDuration = new Trend('video_join_api_duration');
const requestCounter = new Counter('total_requests');
const connectionPoolErrors = new Counter('connection_pool_errors');

// 테스트 설정
export const options = {
  stages: [
    { duration: '10s', target: 20 },   // 10초 동안 20명으로 증가
    { duration: '30s', target: 50 },  // 30초 동안 50명으로 증가
    { duration: '30s', target: 100 }, // 30초 동안 100명으로 증가
    { duration: '60s', target: 100 },  // 60초 동안 100명 유지 (Connection Pool 테스트)
    { duration: '30s', target: 150 }, // 30초 동안 150명으로 증가 (고부하 테스트)
    { duration: '30s', target: 150 },   // 30초 동안 150명 유지
    { duration: '10s', target: 0 },    // 10초 동안 0명으로 감소
  ],
  thresholds: {
    'http_req_duration': ['p(95)<3000', 'p(99)<5000'], // 95%는 3초 이하, 99%는 5초 이하
    'http_req_failed': ['rate<0.1'], // 에러율 10% 미만 (Connection Pool 고갈 허용)
    'errors': ['rate<0.1'],
    'connection_pool_errors': ['count<100'], // Connection Pool 에러 100개 미만
  },
};

// 테스트 데이터
const testData = config.testData;

// 테스트 실행 전 초기화
export function setup() {
  console.log('=== 영상 시청 세션 시작 API 부하 테스트 시작 ===');
  console.log(`Base URL: ${config.baseUrl}`);
  console.log(`Org ID: ${testData.orgId}`);
  console.log(`Video ID: ${testData.videoId}`);
  console.log('⚠️  로컬 테스트: S3/CloudFront URL 생성은 더미 값으로 반환될 수 있습니다.');
  
  // 로그인하여 토큰 발급
  const token = login(config.baseUrl, testData.email, testData.password);
  if (!token) {
    console.error('로그인 실패 - 테스트를 중단합니다.');
    return null;
  }
  
  console.log('로그인 성공 - 토큰 발급 완료');
  
  // 여러 비디오 ID를 사용할 수 있도록 설정 (실제 테스트 시 여러 비디오 ID 필요)
  const videoIds = testData.videoIds ? testData.videoIds.split(',') : [testData.videoId];
  
  return { token, videoIds };
}

// 각 VU가 실행하는 메인 함수
export default function (data) {
  const token = data ? data.token : null;
  const videoIds = data ? data.videoIds : [testData.videoId];
  
  if (!token) {
    console.error('토큰이 없습니다. 테스트를 건너뜁니다.');
    return;
  }

  // 랜덤하게 비디오 선택
  const videoId = videoIds[Math.floor(Math.random() * videoIds.length)];

  // 영상 시청 세션 시작 API 호출
  const url = `${config.baseUrl}/${testData.orgId}/video/${videoId}/join`;
  
  const params = {
    headers: {
      ...config.http.headers,
      ...getAuthHeaders(token),
    },
    tags: {
      name: 'Video Join API',
      videoId: videoId,
    },
  };

  const startTime = Date.now();
  const response = http.post(url, null, params);
  const duration = Date.now() - startTime;

  // 메트릭 업데이트
  requestCounter.add(1);
  videoJoinApiDuration.add(duration);
  errorRate.add(response.status >= 400);

  // Connection Pool 에러 감지 (503, 504, 또는 타임아웃)
  if (response.status === 503 || response.status === 504 || response.timings.duration > 30000) {
    connectionPoolErrors.add(1);
  }

  // 응답 검증
  const success = check(response, {
    '영상 세션 시작 API 상태 코드 200 또는 201': (r) => r.status === 200 || r.status === 201,
    '영상 세션 시작 API 응답 시간 < 3초': (r) => r.timings.duration < 3000,
    '영상 세션 시작 API 응답 본문 존재': (r) => r.body && r.body.length > 0,
    '영상 세션 시작 API JSON 파싱 가능': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body && body.data !== undefined;
      } catch (e) {
        return false;
      }
    },
  });

  if (!success && response.status !== 503 && response.status !== 504) {
    console.error(`영상 세션 시작 API 실패: ${response.status} - ${response.body.substring(0, 200)}`);
  }

  // 요청 간 대기 시간 (비디오 시청 시뮬레이션)
  sleep(Math.random() * 3 + 2); // 2-5초 사이 랜덤 대기
}

// 테스트 종료 후 실행
export function teardown(data) {
  console.log('=== 영상 시청 세션 시작 API 부하 테스트 종료 ===');
  if (data) {
    console.log('테스트 완료');
    console.log('Connection Pool 에러 발생 여부를 확인하세요.');
  }
}

// 결과 리포트 생성
export function handleSummary(data) {
  const resultDir = __ENV.RESULT_DIR || 'results';
  const prefix = __ENV.RESULT_PREFIX || 'video-join-api';
  const ts = new Date().toISOString().replace(/[:.]/g, '-').substring(0, 19);
  return {
    [`${resultDir}/${prefix}-${ts}.html`]: htmlReport(data, { title: '영상 시청 세션 API 부하 테스트 리포트' }),
    [`${resultDir}/${prefix}-${ts}-summary.json`]: JSON.stringify(data, null, 2),
    stdout: textSummary(data, { indent: '  ', enableColors: true }),
  };
}
