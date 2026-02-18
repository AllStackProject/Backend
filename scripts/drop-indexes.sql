-- 인덱스 롤백 스크립트 (add-indexes.sql 역연산)
-- 모든 커스텀 인덱스를 삭제하여 인덱스 적용 전 상태로 복원

-- 1. History 테이블
DROP INDEX IF EXISTS idx_history_member_last_watched;
DROP INDEX IF EXISTS idx_history_member_video;
DROP INDEX IF EXISTS idx_history_video_member;
DROP INDEX IF EXISTS idx_history_member_completed;

-- 2. Video 테이블
DROP INDEX IF EXISTS idx_video_org_status_created;
DROP INDEX IF EXISTS idx_video_org_creator_status;
DROP INDEX IF EXISTS idx_video_org_title_status;
DROP INDEX IF EXISTS idx_video_video_key;

-- 3. Video_Member_Group_Mapping 테이블
DROP INDEX IF EXISTS idx_video_member_group_mapping_video;
DROP INDEX IF EXISTS idx_video_member_group_mapping_group;

-- 4. Member_Group_Mapping 테이블
DROP INDEX IF EXISTS idx_member_group_mapping_member;
DROP INDEX IF EXISTS idx_member_group_mapping_group;

-- 5. Video_Category_Mapping 테이블
DROP INDEX IF EXISTS idx_video_category_mapping_video;
DROP INDEX IF EXISTS idx_video_category_mapping_category;

-- 6. Scrap 테이블
DROP INDEX IF EXISTS idx_scrap_member_video;

-- 7. Member 테이블
DROP INDEX IF EXISTS idx_member_org_status;
DROP INDEX IF EXISTS idx_member_user_status;

-- 8. Notice_Member_Group_Mapping 테이블
DROP INDEX IF EXISTS idx_notice_member_group_mapping_notice;

-- 9. Comment 테이블
DROP INDEX IF EXISTS idx_comment_video_status;
DROP INDEX IF EXISTS idx_comment_parent;
