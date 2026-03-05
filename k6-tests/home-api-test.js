import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';
import { htmlReport } from 'https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js';
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.1.0/index.js';
import { config } from './shared/config.js';
import { login, getAuthHeaders, getToken, setToken } from './shared/auth.js';

// 커스텀 메트릭
const errorRate = new Rate('errors');
const homeApiDuration = new Trend('home_api_duration');
const requestCounter = new Counter('total_requests');

// 테스트 설정
export const options = {
  stages: config.loadTest.stages,
  thresholds: {
    'http_req_duration': ['p(95)<2000', 'p(99)<5000'], // 95%는 2초 이하, 99%는 5초 이하
    'http_req_failed': ['rate<0.05'], // 에러율 5% 미만
    'errors': ['rate<0.05'],
  },
};

// 테스트 데이터
const testData = config.testData;

// 테스트 실행 전 초기화 (한 번만 실행)
export function setup() {
  console.log('=== 홈 조회 API 부하 테스트 시작 ===');
  console.log(`Base URL: ${config.baseUrl}`);
  console.log(`Org ID: ${testData.orgId}`);
  console.log('⚠️  로컬 테스트: AWS S3/Gemini AI는 사용하지 않습니다.');
  
  // 로그인하여 토큰 발급
  const token = login(config.baseUrl, testData.email, testData.password, testData.orgId);
  if (!token) {
    console.error('로그인 실패 - 테스트를 중단합니다.');
    return null;
  }
  
  console.log('로그인 성공 - 토큰 발급 완료');
  return { token };
}

// 각 VU가 실행하는 메인 함수
export default function (data) {
  const token = data ? data.token : null;
  
  if (!token) {
    console.error('토큰이 없습니다. 테스트를 건너뜁니다.');
    return;
  }

  // 홈 조회 API 호출
  const filters = ['RECENT', 'POPULAR', 'RECOMMEND'];
  const filter = filters[Math.floor(Math.random() * filters.length)];
  const url = `${config.baseUrl}/${testData.orgId}/home?filter=${filter}`;
  
  const params = {
    headers: {
      ...config.http.headers,
      ...getAuthHeaders(token),
    },
    tags: {
      name: 'Home API',
      filter: filter,
    },
  };

  const startTime = Date.now();
  const response = http.get(url, params);
  const duration = Date.now() - startTime;

  // 메트릭 업데이트
  requestCounter.add(1);
  homeApiDuration.add(duration);
  errorRate.add(response.status >= 400);

  // 응답 검증
  const success = check(response, {
    '홈 조회 API 상태 코드 200': (r) => r.status === 200,
    '홈 조회 API 응답 시간 < 2초': (r) => r.timings.duration < 2000,
    '홈 조회 API 응답 본문 존재': (r) => r.body && r.body.length > 0,
    '홈 조회 API JSON 파싱 가능': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body && body.data !== undefined;
      } catch (e) {
        return false;
      }
    },
  });

  if (!success) {
    console.error(`홈 조회 API 실패: ${response.status} - ${response.body.substring(0, 200)}`);
  }

  // 요청 간 대기 시간 (실제 사용자 행동 시뮬레이션)
  sleep(Math.random() * 2 + 1); // 1-3초 사이 랜덤 대기
}

// 테스트 종료 후 실행 (요약 정보 출력)
export function teardown(data) {
  console.log('=== 홈 조회 API 부하 테스트 종료 ===');
  if (data) {
    console.log('테스트 완료');
  }
}

// 결과 리포트 생성
export function handleSummary(data) {
  const resultDir = __ENV.RESULT_DIR || 'results';
  const prefix = __ENV.RESULT_PREFIX || 'home-api';
  const ts = new Date().toISOString().replace(/[:.]/g, '-').substring(0, 19);
  return {
    [`${resultDir}/${prefix}-${ts}.html`]: htmlReport(data, { title: '홈 조회 API 부하 테스트 리포트' }),
    [`${resultDir}/${prefix}-${ts}-summary.json`]: JSON.stringify(data, null, 2),
    stdout: textSummary(data, { indent: '  ', enableColors: true }),
  };
}
