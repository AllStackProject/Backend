-- ============================================
-- Privideo 부하 테스트용 대용량 데이터 삽입 SQL
-- ============================================
-- 데이터 규모:
--   - 사용자: 100명
--   - 조직: 3개
--   - 멤버: 조직당 50명 (총 150명)
--   - 멤버 그룹: 조직당 5개 (총 15개)
--   - 비디오: 조직당 500개 (총 1,500개)
--   - 카테고리: 멤버 그룹당 5개 (총 75개)
--   - 시청 기록: 멤버당 약 50개 (총 7,500개+)
--   - 스크랩: 약 1,000개
-- ============================================

-- 기존 데이터 삭제 (필요시 주석 해제)
-- TRUNCATE TABLE scrap, history, video_category_mapping, video_member_group_mapping, video, category, member_group_mapping, member_group, member, organization, users RESTART IDENTITY CASCADE;

-- ============================================
-- 1. 사용자 생성 (100명)
-- ============================================
-- 비밀번호: password123 (BCrypt 해시)
-- BCrypt 해시 값: $2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqGU2gB5rTn5MHLAtvNHMKQOjy.mW

INSERT INTO users (id, name, email, password, gender, phone_number, age, created_at, updated_at, status)
SELECT 
    nextval('users_seq'),
    '테스트유저' || seq,
    'testuser' || seq || '@example.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqGU2gB5rTn5MHLAtvNHMKQOjy.mW',
    CASE WHEN seq % 2 = 0 THEN 'MALE' ELSE 'FEMALE' END,
    '010-' || LPAD((1000 + seq)::text, 4, '0') || '-' || LPAD((1000 + seq)::text, 4, '0'),
    20 + (seq % 30),
    NOW() - INTERVAL '1 day' * (seq % 30),
    NOW(),
    'ACTIVE'
FROM generate_series(1, 100) AS seq
ON CONFLICT (email) DO NOTHING;

-- 테스트용 메인 사용자 (로그인용)
INSERT INTO users (id, name, email, password, gender, phone_number, age, created_at, updated_at, status)
VALUES (
    nextval('users_seq'),
    '테스트관리자',
    'test@example.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqGU2gB5rTn5MHLAtvNHMKQOjy.mW',
    'MALE',
    '010-0000-0000',
    30,
    NOW(),
    NOW(),
    'ACTIVE'
)
ON CONFLICT (email) DO NOTHING;

-- ============================================
-- 2. 조직 생성 (3개)
-- ============================================

INSERT INTO organization (id, user_id, name, img_url, description, created_at, updated_at, status)
SELECT 
    nextval('organization_seq'),
    (SELECT id FROM users WHERE email = 'test@example.com'),
    '테스트조직' || seq,
    'org-images/org' || seq || '.png',
    '부하 테스트를 위한 테스트 조직 ' || seq || '입니다.',
    NOW() - INTERVAL '1 day' * seq,
    NOW(),
    'ACTIVE'
FROM generate_series(1, 3) AS seq
ON CONFLICT (name) DO NOTHING;

-- ============================================
-- 3. 멤버 생성 (조직당 50명)
-- ============================================

-- 조직 1의 멤버 (관리자 포함)
INSERT INTO member (id, user_id, organization_id, nickname, is_admin, join_status, permission_code, created_at, updated_at, status)
SELECT 
    nextval('member_seq'),
    u.id,
    (SELECT id FROM organization WHERE name = '테스트조직1'),
    '멤버_조직1_' || ROW_NUMBER() OVER (ORDER BY u.id),
    CASE WHEN ROW_NUMBER() OVER (ORDER BY u.id) <= 3 THEN true ELSE false END,
    'APPROVED',
    CASE WHEN ROW_NUMBER() OVER (ORDER BY u.id) <= 3 THEN 15 ELSE 0 END,
    NOW() - INTERVAL '1 hour' * ROW_NUMBER() OVER (ORDER BY u.id),
    NOW(),
    'ACTIVE'
FROM users u
WHERE u.id IN (SELECT id FROM users ORDER BY id LIMIT 50)
ON CONFLICT DO NOTHING;

-- 조직 2의 멤버
INSERT INTO member (id, user_id, organization_id, nickname, is_admin, join_status, permission_code, created_at, updated_at, status)
SELECT 
    nextval('member_seq'),
    u.id,
    (SELECT id FROM organization WHERE name = '테스트조직2'),
    '멤버_조직2_' || ROW_NUMBER() OVER (ORDER BY u.id),
    CASE WHEN ROW_NUMBER() OVER (ORDER BY u.id) <= 3 THEN true ELSE false END,
    'APPROVED',
    CASE WHEN ROW_NUMBER() OVER (ORDER BY u.id) <= 3 THEN 15 ELSE 0 END,
    NOW() - INTERVAL '1 hour' * ROW_NUMBER() OVER (ORDER BY u.id),
    NOW(),
    'ACTIVE'
FROM users u
WHERE u.id IN (SELECT id FROM users ORDER BY id OFFSET 25 LIMIT 50)
ON CONFLICT DO NOTHING;

-- 조직 3의 멤버
INSERT INTO member (id, user_id, organization_id, nickname, is_admin, join_status, permission_code, created_at, updated_at, status)
SELECT 
    nextval('member_seq'),
    u.id,
    (SELECT id FROM organization WHERE name = '테스트조직3'),
    '멤버_조직3_' || ROW_NUMBER() OVER (ORDER BY u.id),
    CASE WHEN ROW_NUMBER() OVER (ORDER BY u.id) <= 3 THEN true ELSE false END,
    'APPROVED',
    CASE WHEN ROW_NUMBER() OVER (ORDER BY u.id) <= 3 THEN 15 ELSE 0 END,
    NOW() - INTERVAL '1 hour' * ROW_NUMBER() OVER (ORDER BY u.id),
    NOW(),
    'ACTIVE'
FROM users u
WHERE u.id IN (SELECT id FROM users ORDER BY id OFFSET 50 LIMIT 50)
ON CONFLICT DO NOTHING;

-- 테스트 관리자를 조직1에 추가
INSERT INTO member (id, user_id, organization_id, nickname, is_admin, join_status, permission_code, created_at, updated_at, status)
SELECT 
    nextval('member_seq'),
    (SELECT id FROM users WHERE email = 'test@example.com'),
    (SELECT id FROM organization WHERE name = '테스트조직1'),
    '테스트관리자',
    true,
    'APPROVED',
    15,
    NOW(),
    NOW(),
    'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1 FROM member 
    WHERE user_id = (SELECT id FROM users WHERE email = 'test@example.com')
    AND organization_id = (SELECT id FROM organization WHERE name = '테스트조직1')
);

-- ============================================
-- 4. 멤버 그룹 생성 (조직당 5개)
-- ============================================

INSERT INTO member_group (id, organization_id, name, created_at, updated_at, status)
SELECT 
    nextval('member_group_seq'),
    o.id,
    o.name || '_그룹' || g.seq,
    NOW(),
    NOW(),
    'ACTIVE'
FROM organization o
CROSS JOIN generate_series(1, 5) AS g(seq)
ON CONFLICT DO NOTHING;

-- ============================================
-- 5. 멤버 그룹 매핑 (각 멤버를 1~3개 그룹에 할당)
-- ============================================

INSERT INTO member_group_mapping (id, member_id, member_group_id, created_at, updated_at, status)
SELECT 
    nextval('member_group_mapping_seq'),
    sub.member_id,
    sub.member_group_id,
    NOW(),
    NOW(),
    'ACTIVE'
FROM (
    SELECT m.id as member_id, mg.id as member_group_id,
           ROW_NUMBER() OVER (PARTITION BY m.id ORDER BY RANDOM()) as rn
    FROM member m
    JOIN member_group mg ON mg.organization_id = m.organization_id
) sub
WHERE sub.rn <= 2  -- 멤버당 최대 2개 그룹에 속함
ON CONFLICT DO NOTHING;

-- ============================================
-- 6. 카테고리 생성 (멤버 그룹당 5개)
-- ============================================

INSERT INTO category (id, title, member_group_id, created_at, updated_at, status)
SELECT 
    nextval('category_seq'),
    mg.name || '_카테고리' || c.seq,
    mg.id,
    NOW(),
    NOW(),
    'ACTIVE'
FROM member_group mg
CROSS JOIN generate_series(1, 5) AS c(seq)
ON CONFLICT DO NOTHING;

-- ============================================
-- 7. 비디오 생성 (조직당 500개, 총 1,500개)
-- ============================================

INSERT INTO video (
    id, organization_id, member_id, title, description, 
    video_url, thumbnail_url, hls_prefix, whole_time, 
    is_comment, ai_function_type, ai_feedback, ai_summary, 
    expired_at, watch_cnt, quit_cnt, upload_status,
    created_at, updated_at, status
)
SELECT 
    nextval('video_seq'),
    o.id,
    (SELECT id FROM member WHERE organization_id = o.id AND is_admin = true LIMIT 1),
    '테스트 비디오 ' || o.name || ' #' || v.seq,
    '이것은 부하 테스트를 위한 테스트 비디오 ' || v.seq || '의 설명입니다. ' ||
    '다양한 주제의 영상 컨텐츠를 포함하고 있으며, 테스트 목적으로 생성되었습니다.',
    'videos/' || o.id || '/video_' || v.seq || '.mp4',
    'thumbnails/' || o.id || '/thumb_' || v.seq || '.jpg',
    'hls/' || o.id || '/video_' || v.seq || '/',
    300 + (v.seq % 600),  -- 5분 ~ 15분
    CASE WHEN v.seq % 3 = 0 THEN true ELSE false END,
    CASE 
        WHEN v.seq % 4 = 0 THEN 'SUMMARY'
        WHEN v.seq % 4 = 1 THEN 'FEEDBACK'
        WHEN v.seq % 4 = 2 THEN 'QUIZ'
        ELSE 'NONE'
    END,
    CASE WHEN v.seq % 4 IN (0, 1) THEN 'AI가 생성한 피드백/요약 내용입니다.' ELSE NULL END,
    CASE WHEN v.seq % 4 = 0 THEN 'AI가 생성한 요약 내용입니다. 이 비디오는 다양한 주제를 다루고 있습니다.' ELSE NULL END,
    CURRENT_DATE + INTERVAL '1 year',
    (v.seq % 1000),  -- 조회수
    (v.seq % 100),   -- 중도 이탈수
    'COMPLETE',
    NOW() - INTERVAL '1 day' * (v.seq % 90),  -- 최근 90일 내 생성
    NOW(),
    'ACTIVE'
FROM organization o
CROSS JOIN generate_series(1, 500) AS v(seq);

-- ============================================
-- 8. 비디오-멤버그룹 매핑 (일부 비디오만 그룹 제한)
-- ============================================

INSERT INTO video_member_group_mapping (id, member_group_id, video_id, created_at, updated_at, status)
SELECT 
    nextval('video_member_group_mapping_seq'),
    sub.member_group_id,
    sub.video_id,
    NOW(),
    NOW(),
    'ACTIVE'
FROM (
    SELECT v.id as video_id, mg.id as member_group_id,
           ROW_NUMBER() OVER (PARTITION BY v.id ORDER BY RANDOM()) as rn
    FROM video v
    JOIN member_group mg ON mg.organization_id = v.organization_id
    WHERE v.id IN (
        SELECT id FROM video ORDER BY RANDOM() LIMIT 300  -- 20%의 비디오 (1500 * 0.2 = 300)
    )
) sub
WHERE sub.rn <= 2  -- 비디오당 최대 2개 그룹에 매핑
ON CONFLICT DO NOTHING;

-- ============================================
-- 9. 비디오-카테고리 매핑
-- ============================================

INSERT INTO video_category_mapping (id, video_id, category_id, created_at, updated_at, status)
SELECT 
    nextval('video_category_mapping_seq'),
    sub.video_id,
    sub.category_id,
    NOW(),
    NOW(),
    'ACTIVE'
FROM (
    SELECT v.id as video_id, c.id as category_id,
           ROW_NUMBER() OVER (PARTITION BY v.id ORDER BY RANDOM()) as rn
    FROM video v
    JOIN category c ON c.member_group_id IN (
        SELECT mg.id FROM member_group mg WHERE mg.organization_id = v.organization_id
    )
) sub
WHERE sub.rn <= 3  -- 비디오당 최대 3개 카테고리에 매핑
ON CONFLICT DO NOTHING;

-- ============================================
-- 10. 시청 기록 생성 (대용량 - 멤버당 약 50개)
-- ============================================

INSERT INTO history (
    id, member_id, video_id, watch_rate, recent_position_sec,
    started_at, had_end, is_complete, completed_at, last_watched_at,
    created_at, updated_at, status
)
SELECT 
    nextval('history_seq'),
    sub.member_id,
    sub.video_id,
    (RANDOM() * 100)::INTEGER,
    (RANDOM() * sub.whole_time)::INTEGER,
    NOW() - INTERVAL '1 day' * (RANDOM() * 30)::INTEGER,
    CASE WHEN RANDOM() > 0.3 THEN true ELSE false END,
    CASE WHEN RANDOM() > 0.5 THEN true ELSE false END,
    CASE WHEN RANDOM() > 0.5 THEN NOW() - INTERVAL '1 day' * (RANDOM() * 10)::INTEGER ELSE NULL END,
    NOW() - INTERVAL '1 hour' * (RANDOM() * 720)::INTEGER,
    NOW(),
    NOW(),
    'ACTIVE'
FROM (
    SELECT m.id as member_id, v.id as video_id, v.whole_time,
           ROW_NUMBER() OVER (PARTITION BY m.id ORDER BY RANDOM()) as rn
    FROM member m
    JOIN video v ON v.organization_id = m.organization_id
) sub
WHERE sub.rn <= 50  -- 멤버당 최대 50개의 시청 기록
ON CONFLICT DO NOTHING;

-- ============================================
-- 11. 스크랩 생성 (약 1,000개)
-- ============================================

INSERT INTO scrap (id, member_id, video_id, created_at, updated_at, status)
SELECT 
    nextval('scrap_seq'),
    sub.member_id,
    sub.video_id,
    NOW() - INTERVAL '1 day' * (RANDOM() * 30)::INTEGER,
    NOW(),
    'ACTIVE'
FROM (
    SELECT m.id as member_id, v.id as video_id,
           ROW_NUMBER() OVER (PARTITION BY m.id ORDER BY RANDOM()) as rn
    FROM member m
    JOIN video v ON v.organization_id = m.organization_id
) sub
WHERE sub.rn <= 10  -- 멤버당 최대 10개 스크랩
ON CONFLICT DO NOTHING;

-- ============================================
-- 12. 데이터 검증 쿼리
-- ============================================

SELECT 'Users' as table_name, COUNT(*) as count FROM users
UNION ALL SELECT 'Organizations', COUNT(*) FROM organization
UNION ALL SELECT 'Members', COUNT(*) FROM member
UNION ALL SELECT 'Member Groups', COUNT(*) FROM member_group
UNION ALL SELECT 'Member Group Mappings', COUNT(*) FROM member_group_mapping
UNION ALL SELECT 'Categories', COUNT(*) FROM category
UNION ALL SELECT 'Videos', COUNT(*) FROM video
UNION ALL SELECT 'Video Member Group Mappings', COUNT(*) FROM video_member_group_mapping
UNION ALL SELECT 'Video Category Mappings', COUNT(*) FROM video_category_mapping
UNION ALL SELECT 'Histories', COUNT(*) FROM history
UNION ALL SELECT 'Scraps', COUNT(*) FROM scrap;

-- ============================================
-- 테스트 계정 정보 출력
-- ============================================

SELECT 
    '=== 테스트 계정 정보 ===' as info
UNION ALL
SELECT 'Email: test@example.com'
UNION ALL
SELECT 'Password: password123'
UNION ALL
SELECT 'Org ID: ' || (SELECT id::text FROM organization WHERE name = '테스트조직1')
UNION ALL
SELECT 'Member ID: ' || (SELECT m.id::text FROM member m JOIN users u ON m.user_id = u.id WHERE u.email = 'test@example.com' LIMIT 1)
UNION ALL
SELECT 'Video ID: ' || (SELECT id::text FROM video WHERE organization_id = (SELECT id FROM organization WHERE name = '테스트조직1') LIMIT 1);
