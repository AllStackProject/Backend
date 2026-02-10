/**
 * 로그인하여 JWT 토큰을 발급받습니다.
 * @param {string} baseUrl - API 서버 기본 URL
 * @param {string} email - 사용자 이메일
 * @param {string} password - 사용자 비밀번호
 * @returns {string|null} JWT 토큰 또는 null
 */
export function login(baseUrl, email, password) {
    const loginUrl = `${baseUrl}/user/login`;
    const payload = JSON.stringify({
        email: email,
        password: password,
    });

    const response = http.post(loginUrl, payload, {
        headers: {'Content-Type': 'application/json'},
    });

    if (response.status === 200) {
        // Authorization 헤더에서 토큰 추출
        const authHeader = response.headers['Authorization'] || response.headers['authorization'];
        if (authHeader && authHeader.startsWith('Bearer ')) {
            return authHeader.substring(7); // "Bearer " 제거
        }
        // 응답 본문에서 토큰 확인 (필요한 경우)
        try {
            const body = JSON.parse(response.body);
            if (body.data && body.data.token) {
                return body.data.token;
            }
        } catch (e) {
            // JSON 파싱 실패 시 무시
        }
    }

    console.error(`로그인 실패: ${response.status} - ${response.body}`);
    return null;
}

/**
 * JWT 토큰을 사용하여 인증 헤더를 생성합니다.
 * @param {string} token - JWT 토큰
 * @returns {object} Authorization 헤더가 포함된 객체
 */
export function getAuthHeaders(token) {
    if (!token) {
        return {};
    }
    return {
        'Authorization': `Bearer ${token}`,
    };
}

/**
 * 토큰을 환경 변수나 공유 데이터에서 가져옵니다.
 * @param {object} sharedData - k6 공유 데이터 객체
 * @returns {string|null} JWT 토큰 또는 null
 */
export function getToken(sharedData) {
    if (sharedData && sharedData.token) {
        return sharedData.token;
    }
    return null;
}

/**
 * 토큰을 공유 데이터에 저장합니다.
 * @param {object} sharedData - k6 공유 데이터 객체
 * @param {string} token - JWT 토큰
 */
export function setToken(sharedData, token) {
    if (sharedData) {
        sharedData.token = token;
    }
}
