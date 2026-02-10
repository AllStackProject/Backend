-- ============================================
-- Privideo 테스트 데이터 초기화 SQL
-- ============================================
-- 주의: 이 스크립트는 모든 데이터를 삭제합니다!
-- 운영 환경에서는 절대 실행하지 마세요.
-- ============================================

-- 트랜잭션 시작
BEGIN;

-- 외래 키 제약 조건을 고려한 순서로 삭제
-- 자식 테이블부터 삭제

-- 1. Scrap 삭제
TRUNCATE TABLE scrap RESTART IDENTITY CASCADE;

-- 2. History 삭제
TRUNCATE TABLE history RESTART IDENTITY CASCADE;

-- 3. Video_Category_Mapping 삭제
TRUNCATE TABLE video_category_mapping RESTART IDENTITY CASCADE;

-- 4. Video_Member_Group_Mapping 삭제
TRUNCATE TABLE video_member_group_mapping RESTART IDENTITY CASCADE;

-- 5. Video 삭제
TRUNCATE TABLE video RESTART IDENTITY CASCADE;

-- 6. Category 삭제
TRUNCATE TABLE category RESTART IDENTITY CASCADE;

-- 7. Member_Group_Mapping 삭제
TRUNCATE TABLE member_group_mapping RESTART IDENTITY CASCADE;

-- 8. Member_Group 삭제
TRUNCATE TABLE member_group RESTART IDENTITY CASCADE;

-- 9. Member 삭제
TRUNCATE TABLE member RESTART IDENTITY CASCADE;

-- 10. Organization 삭제
TRUNCATE TABLE organization RESTART IDENTITY CASCADE;

-- 11. Users 삭제
TRUNCATE TABLE users RESTART IDENTITY CASCADE;

-- 트랜잭션 커밋
COMMIT;

-- 초기화 완료 확인
SELECT 'Users' as table_name, COUNT(*) as count FROM users
UNION ALL SELECT 'Organizations', COUNT(*) FROM organization
UNION ALL SELECT 'Members', COUNT(*) FROM member
UNION ALL SELECT 'Videos', COUNT(*) FROM video
UNION ALL SELECT 'Histories', COUNT(*) FROM history
UNION ALL SELECT 'Scraps', COUNT(*) FROM scrap;

SELECT '=== 데이터 초기화 완료 ===' as result;
