import http from 'k6/http';

/**
 * 로그인하여 JWT 토큰을 발급받습니다.
 * @param {string} baseUrl - API 서버 기본 URL
 * @param {string} email - 사용자 이메일
 * @param {string} password - 사용자 비밀번호
 * @returns {string|null} JWT 토큰 또는 null
 */
export function login(baseUrl, email, password, orgId) {
    // Step 1: 로그인 → BOOTSTRAP 토큰 발급
    const loginUrl = `${baseUrl}/user/login`;
    const payload = JSON.stringify({
        email: email,
        password: password,
    });

    const loginRes = http.post(loginUrl, payload, {
        headers: {'Content-Type': 'application/json'},
    });

    if (loginRes.status !== 200) {
        console.error(`로그인 실패: ${loginRes.status} - ${loginRes.body}`);
        return null;
    }

    const authHeader = loginRes.headers['Authorization'] || loginRes.headers['authorization'];
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
        console.error('로그인 응답에 Authorization 헤더가 없습니다.');
        return null;
    }
    const bootstrapToken = authHeader.substring(7);

    // Step 2: 조직 선택 → ORG 토큰 발급
    const selectOrgUrl = `${baseUrl}/orgs/${orgId || 1}`;
    const selectRes = http.patch(selectOrgUrl, null, {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${bootstrapToken}`,
        },
    });

    if (selectRes.status !== 200) {
        console.error(`조직 선택 실패: ${selectRes.status} - ${selectRes.body}`);
        return null;
    }

    const orgAuthHeader = selectRes.headers['Authorization'] || selectRes.headers['authorization'];
    if (orgAuthHeader && orgAuthHeader.startsWith('Bearer ')) {
        return orgAuthHeader.substring(7);
    }

    console.error('조직 선택 응답에 ORG 토큰이 없습니다.');
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
