-- 성능 개선을 위한 인덱스 추가 스크립트
-- 실행 전: 기존 인덱스 확인 및 중복 방지
-- 실행 후: EXPLAIN ANALYZE로 쿼리 성능 확인

-- ============================================
-- 1. History 테이블 인덱스
-- ============================================

-- 멤버별 시청 기록 조회 및 정렬 최적화
-- 사용 쿼리: HistoryRepositoryImpl.findByMemberId()
-- WHERE: member_id, join_status, upload_status
-- ORDER BY: last_watched_at DESC
CREATE INDEX IF NOT EXISTS idx_history_member_last_watched 
ON "History" (member_id, last_watched_at DESC)
WHERE status = 'ACTIVE';

-- 멤버와 비디오 조합 조회 최적화
-- 사용 쿼리: HistoryRepository.findByMemberIdAndVideoId()
CREATE INDEX IF NOT EXISTS idx_history_member_video 
ON "History" (member_id, video_id)
WHERE status = 'ACTIVE';

-- 비디오별 시청 기록 조회 최적화
-- 사용 쿼리: HistoryRepositoryImpl.findVideoWatchLogByVideoId()
CREATE INDEX IF NOT EXISTS idx_history_video_member 
ON "History" (video_id, member_id, last_watched_at DESC)
WHERE status = 'ACTIVE';

-- 완료된 시청 기록 기간별 조회 최적화
-- 사용 쿼리: HistoryRepositoryImpl.findTopCategoriesByMemberIdWithinPeriod()
CREATE INDEX IF NOT EXISTS idx_history_member_completed 
ON "History" (member_id, is_complete, completed_at)
WHERE status = 'ACTIVE' AND is_complete = true;

-- ============================================
-- 2. Video 테이블 인덱스
-- ============================================

-- 조직별 비디오 목록 조회 최적화
-- 사용 쿼리: VideoRepositoryImpl.findHomeVideos()
-- WHERE: organization_id, upload_status, join_status, status
-- ORDER BY: created_at DESC, watch_cnt DESC
CREATE INDEX IF NOT EXISTS idx_video_org_status_created 
ON "Video" (organization_id, upload_status, created_at DESC)
WHERE status = 'ACTIVE';

-- 조직별 비디오 조회 (크리에이터 필터링 포함)
-- 사용 쿼리: VideoRepositoryImpl.findByOrgIdAndCreatorId()
CREATE INDEX IF NOT EXISTS idx_video_org_creator_status 
ON "Video" (organization_id, member_id, upload_status, created_at DESC)
WHERE status = 'ACTIVE';

-- 비디오 제목 검색 최적화
-- 사용 쿼리: VideoRepositoryImpl.findSearchVideos()
-- WHERE: organization_id, title (LIKE), upload_status
CREATE INDEX IF NOT EXISTS idx_video_org_title_status 
ON "Video" (organization_id, upload_status, title)
WHERE status = 'ACTIVE';

-- 비디오 키로 조회 (인코딩 결과 업데이트용)
-- 사용 쿼리: VideoRepository.findByVideoKey()
CREATE INDEX IF NOT EXISTS idx_video_video_key 
ON "Video" (video_url)
WHERE status = 'ACTIVE';

-- ============================================
-- 3. Video_Member_Group_Mapping 테이블 인덱스
-- ============================================

-- 비디오별 멤버 그룹 매핑 조회 최적화
-- 사용 쿼리: VideoMemberGroupMappingRepository.findAllByVideoId()
CREATE INDEX IF NOT EXISTS idx_video_member_group_mapping_video 
ON "Video_Member_Group_Mapping" (video_id, status)
WHERE status = 'ACTIVE';

-- 멤버 그룹별 비디오 매핑 조회 최적화
-- 사용 쿼리: 비디오 접근 권한 확인 쿼리
CREATE INDEX IF NOT EXISTS idx_video_member_group_mapping_group 
ON "Video_Member_Group_Mapping" (member_group_id, video_id, status)
WHERE status = 'ACTIVE';

-- ============================================
-- 4. Member_Group_Mapping 테이블 인덱스
-- ============================================

-- 멤버별 그룹 매핑 조회 최적화
-- 사용 쿼리: MemberGroupMappingRepository.findAllByMemberId()
CREATE INDEX IF NOT EXISTS idx_member_group_mapping_member 
ON "Member_Group_Mapping" (member_id, member_group_id, status)
WHERE status = 'ACTIVE';

-- 그룹별 멤버 매핑 조회 최적화
CREATE INDEX IF NOT EXISTS idx_member_group_mapping_group 
ON "Member_Group_Mapping" (member_group_id, member_id, status)
WHERE status = 'ACTIVE';

-- ============================================
-- 5. Video_Category_Mapping 테이블 인덱스
-- ============================================

-- 비디오별 카테고리 매핑 조회 최적화
-- 사용 쿼리: VideoCategoryMappingRepository.findAllByVideoId()
CREATE INDEX IF NOT EXISTS idx_video_category_mapping_video 
ON "Video_Category_Mapping" (video_id, category_id, status)
WHERE status = 'ACTIVE';

-- 카테고리별 비디오 매핑 조회 최적화
CREATE INDEX IF NOT EXISTS idx_video_category_mapping_category 
ON "Video_Category_Mapping" (category_id, video_id, status)
WHERE status = 'ACTIVE';

-- ============================================
-- 6. Scrap 테이블 인덱스
-- ============================================

-- 멤버와 비디오 조합으로 스크랩 확인 최적화
-- 사용 쿼리: ScrapRepository.existsByMemberIdAndVideoId()
CREATE INDEX IF NOT EXISTS idx_scrap_member_video 
ON "Scrap" (member_id, video_id, status)
WHERE status = 'ACTIVE';

-- ============================================
-- 7. Member 테이블 인덱스
-- ============================================

-- 조직별 멤버 조회 최적화
-- 사용 쿼리: MemberRepository.findByIdAndOrganizationIdAndStatus()
CREATE INDEX IF NOT EXISTS idx_member_org_status 
ON "member" (organization_id, id, status, join_status)
WHERE status = 'ACTIVE';

-- 사용자별 멤버 조회 최적화
-- 사용 쿼리: MemberRepository.findByUserId()
CREATE INDEX IF NOT EXISTS idx_member_user_status 
ON "member" (user_id, status, join_status)
WHERE status = 'ACTIVE';

-- ============================================
-- 8. Notice_Member_Group_Mapping 테이블 인덱스
-- ============================================

-- 공지사항별 멤버 그룹 매핑 조회 최적화
-- 사용 쿼리: NoticeMemberGroupMappingRepository.findAllByNoticeId()
CREATE INDEX IF NOT EXISTS idx_notice_member_group_mapping_notice 
ON "Notice_Member_Group_Mapping" (notice_id, member_group_id, status)
WHERE status = 'ACTIVE';

-- ============================================
-- 9. Comment 테이블 인덱스
-- ============================================

-- 비디오별 댓글 조회 최적화
-- 사용 쿼리: CommentRepository.findByVideoId()
CREATE INDEX IF NOT EXISTS idx_comment_video_status 
ON "Comment" (video_id, status, created_at DESC)
WHERE status = 'ACTIVE';

-- 부모 댓글별 자식 댓글 조회 최적화
CREATE INDEX IF NOT EXISTS idx_comment_parent 
ON "Comment" (parent_comment_id, status, created_at)
WHERE status = 'ACTIVE' AND is_child = true;

-- ============================================
-- 인덱스 생성 완료 확인
-- ============================================

-- 인덱스 목록 확인 쿼리 (실행 후 확인용)
-- SELECT 
--     schemaname,
--     tablename,
--     indexname,
--     indexdef
-- FROM pg_indexes
-- WHERE tablename IN (
--     'History', 'Video', 'Video_Member_Group_Mapping',
--     'Member_Group_Mapping', 'Video_Category_Mapping',
--     'Scrap', 'member', 'Notice_Member_Group_Mapping', 'Comment'
-- )
-- ORDER BY tablename, indexname;
