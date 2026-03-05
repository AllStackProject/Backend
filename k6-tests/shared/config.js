// k6 테스트 공통 설정
export const config = {
  // API 서버 기본 URL
  baseUrl: __ENV.BASE_URL || 'https://localhost:8080',
  
  // 테스트 데이터
  testData: {
    // 테스트용 사용자 정보 (실제 테스트 시 환경변수로 주입 필요)
    userId: __ENV.USER_ID || 1,
    memberId: __ENV.MEMBER_ID || 1,
    orgId: __ENV.ORG_ID || 1,
    videoId: __ENV.VIDEO_ID || 1,
    
    // 로그인 정보 (토큰 발급용)
    email: __ENV.EMAIL || 'test@example.com',
    password: __ENV.PASSWORD || 'password123',
  },
  
  // 부하 테스트 설정
  loadTest: {
    // Virtual Users (동시 사용자 수)
    vus: parseInt(__ENV.VUS) || 10,
    
    // 테스트 지속 시간
    duration: __ENV.DURATION || '30s',
    
    // Ramp-up 설정 (점진적 부하 증가)
    stages: [
      { duration: '10s', target: 10 },  // 10초 동안 10명으로 증가
      { duration: '30s', target: 50 },   // 30초 동안 50명으로 증가
      { duration: '30s', target: 100 },  // 30초 동안 100명으로 증가
      { duration: '30s', target: 100 },  // 30초 동안 100명 유지
      { duration: '10s', target: 0 },    // 10초 동안 0명으로 감소
    ],
  },
  
  // HTTP 요청 설정
  http: {
    timeout: '30s',
    headers: {
      'Content-Type': 'application/json',
    },
  },
  
  // 결과 출력 설정
  output: {
    // JSON 결과 파일 경로
    jsonPath: __ENV.OUTPUT_JSON || 'results/k6-results.json',
    // CSV 결과 파일 경로
    csvPath: __ENV.OUTPUT_CSV || 'results/k6-results.csv',
  },
};
