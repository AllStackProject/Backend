package app.allstackproject.privideo.domain.video;

import static app.allstackproject.privideo.domain.video.enums.AiFunctionType.NONE;
import static app.allstackproject.privideo.domain.video.enums.AiFunctionType.SUMMARY;
import static app.allstackproject.privideo.domain.video.enums.UploadStatusType.COMPLETE;
import static app.allstackproject.privideo.domain.video.enums.UploadStatusType.FAIL;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.INVALID_AIRFLOW_STATUS;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.MEMBER_NOT_IN_ORGANIZATION;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.PLAY_SESSION_NOT_FOUND;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.VIDEO_ALREADY_WATCHING;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.VIDEO_NOT_ACCESSIBLE;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.VIDEO_NOT_IN_ORGANIZATION;
import static app.allstackproject.privideo.shared.enums.BaseStatusType.ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import app.allstackproject.privideo.domain.admin.dto.ReadAllVideoItem;
import app.allstackproject.privideo.domain.comment.repository.CommentRepository;
import app.allstackproject.privideo.domain.history.entity.History;
import app.allstackproject.privideo.domain.history.repository.HistoryRepository;
import app.allstackproject.privideo.domain.member.entity.Member;
import app.allstackproject.privideo.domain.member.entity.MemberGroup;
import app.allstackproject.privideo.domain.member.entity.MemberGroupMapping;
import app.allstackproject.privideo.domain.member.repository.MemberGroupMappingRepository;
import app.allstackproject.privideo.domain.member.repository.MemberGroupRepository;
import app.allstackproject.privideo.domain.member.repository.MemberRepository;
import app.allstackproject.privideo.domain.organization.dto.enums.OpenScopeType;
import app.allstackproject.privideo.domain.organization.entity.Organization;
import app.allstackproject.privideo.domain.organization.repository.OrganizationRepository;
import app.allstackproject.privideo.domain.quiz.repository.QuizRepository;
import app.allstackproject.privideo.domain.scrap.repository.ScrapRepository;
import app.allstackproject.privideo.domain.video.dto.request.CreateVideoRequest;
import app.allstackproject.privideo.domain.video.dto.request.LeaveVideoSessionInfo;
import app.allstackproject.privideo.domain.video.dto.request.ModifyVideoRequest;
import app.allstackproject.privideo.domain.video.dto.response.CreateVideoResponse;
import app.allstackproject.privideo.domain.video.dto.response.JoinVideoSessionResult;
import app.allstackproject.privideo.domain.video.dto.response.ReadVideoInfoResponse;
import app.allstackproject.privideo.domain.video.entity.Category;
import app.allstackproject.privideo.domain.video.entity.Video;
import app.allstackproject.privideo.domain.video.enums.UploadStatusType;
import app.allstackproject.privideo.domain.video.repository.CategoryRepository;
import app.allstackproject.privideo.domain.video.repository.VideoCategoryMappingRepository;
import app.allstackproject.privideo.domain.video.repository.VideoMemberGroupMappingRepository;
import app.allstackproject.privideo.domain.video.repository.VideoRedisRepository;
import app.allstackproject.privideo.domain.video.repository.VideoRepository;
import app.allstackproject.privideo.domain.video.service.AiFunctionService;
import app.allstackproject.privideo.domain.video.service.LogService;
import app.allstackproject.privideo.domain.video.service.VideoService;
import app.allstackproject.privideo.global.exception.ApiException;
import app.allstackproject.privideo.global.response.SuccessResponse;
import app.allstackproject.privideo.global.util.CdnUrlProvider;
import app.allstackproject.privideo.global.util.S3Util;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class VideoServiceTest {

    @InjectMocks
    private VideoService videoService;

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private MemberGroupRepository memberGroupRepository;
    @Mock
    private VideoRepository videoRepository;
    @Mock
    private HistoryRepository historyRepository;
    @Mock
    private LogService logService;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ScrapRepository scrapRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private S3Util s3Util;
    @Mock
    private CdnUrlProvider cdnUrlProvider;
    @Mock
    private VideoMemberGroupMappingRepository videoMemberGroupMappingRepository;
    @Mock
    private VideoCategoryMappingRepository videoCategoryMappingRepository;
    @Mock
    private QuizRepository quizRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private AiFunctionService aiFunctionService;
    @Mock
    private MemberGroupMappingRepository memberGroupMappingRepository;
    @Mock
    private VideoRedisRepository videoRedisRepository;

    // ===== prepareJoinVideoSession =====
    @Nested
    @DisplayName("prepareJoinVideoSession")
    class PrepareJoinVideoSession {

        @Test
        @DisplayName("정상 진입 - 미완료 history -> create 타입 결과")
        void success_createResult() {
            Long memberId = 1L;
            Long orgId = 10L;
            Long videoId = 100L;

            Member member = mock(Member.class);
            Organization org = mock(Organization.class);
            Video video = mock(Video.class);

            given(memberRepository.findByIdAndOrganizationIdAndStatus(memberId, orgId, ACTIVE))
                    .willReturn(Optional.of(member));

            given(videoRepository.findById(videoId))
                    .willReturn(Optional.of(video));
            given(video.getOrganization()).willReturn(org);
            given(org.getId()).willReturn(orgId);
            given(video.getWholeTime()).willReturn(120L);
            given(video.getHlsPrefix()).willReturn("hls/org-10/uuid");
            given(video.getIsComment()).willReturn(true);
            given(video.getAiFunctionType()).willReturn(NONE);

            given(videoRedisRepository.existsWatchSession(anyString()))
                    .willReturn(false);

            given(memberGroupRepository.isAccessibleToVideo(memberId, videoId))
                    .willReturn(true);

            given(s3Util.generatePlaybackUrl("hls/org-10/uuid"))
                    .willReturn("https://cdn/hls/org-10/uuid/master.m3u8");

            given(logService.getSegViewCounts(eq(videoId), anyInt()))
                    .willReturn(List.of(1L, 2L, 3L));

            given(scrapRepository.existsByMemberIdAndVideoId(memberId, videoId))
                    .willReturn(false);

            given(categoryRepository.findAllByVideoId(videoId))
                    .willReturn(List.of("cat1", "cat2"));

            History history = mock(History.class);
            given(historyRepository.findByMemberIdAndVideoId(memberId, videoId))
                    .willReturn(Optional.of(history));
            given(history.getRecentPositionSec()).willReturn(30L);
            given(history.isComplete()).willReturn(false);

            // when
            JoinVideoSessionResult result = videoService.prepareJoinVideoSession(memberId, orgId, videoId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getPlaybackUrl()).isEqualTo("https://cdn/hls/org-10/uuid/master.m3u8");
            assertThat(result.getSegViewCnts()).containsExactly(1L, 2L, 3L);
            assertThat(result.getIsScrapped()).isFalse();
        }

        @Test
        @DisplayName("완료된 history가 있으면 completed 타입 결과 반환")
        void success_completedResult() {
            Long memberId = 1L;
            Long orgId = 10L;
            Long videoId = 100L;

            Member member = mock(Member.class);
            Organization org = mock(Organization.class);
            Video video = mock(Video.class);

            given(memberRepository.findByIdAndOrganizationIdAndStatus(memberId, orgId, ACTIVE))
                    .willReturn(Optional.of(member));

            given(videoRepository.findById(videoId))
                    .willReturn(Optional.of(video));
            given(video.getOrganization()).willReturn(org);
            given(org.getId()).willReturn(orgId);
            given(video.getWholeTime()).willReturn(60L);
            given(video.getHlsPrefix()).willReturn("hls/org-10/uuid");
            given(video.getIsComment()).willReturn(true);
            given(video.getAiFunctionType()).willReturn(SUMMARY);
            given(video.getAiSummary()).willReturn("요약 내용");

            given(videoRedisRepository.existsWatchSession(anyString()))
                    .willReturn(false);

            given(memberGroupRepository.isAccessibleToVideo(memberId, videoId))
                    .willReturn(true);

            given(s3Util.generatePlaybackUrl("hls/org-10/uuid"))
                    .willReturn("https://cdn/hls/org-10/uuid/master.m3u8");

            given(logService.getSegViewCounts(eq(videoId), anyInt()))
                    .willReturn(List.of(1L, 2L));

            given(scrapRepository.existsByMemberIdAndVideoId(memberId, videoId))
                    .willReturn(true);

            given(categoryRepository.findAllByVideoId(videoId))
                    .willReturn(List.of("cat1"));

            History history = mock(History.class);
            given(historyRepository.findByMemberIdAndVideoId(memberId, videoId))
                    .willReturn(Optional.of(history));
            given(history.getRecentPositionSec()).willReturn(60L);
            given(history.isComplete()).willReturn(true);

            // when
            JoinVideoSessionResult result = videoService.prepareJoinVideoSession(memberId, orgId, videoId);

            // then
            assertThat(result.getWatchCompleted()).isTrue();
            assertThat(result.getVideo().getRecentPositionSec()).isEqualTo(60L);
            assertThat(result.getAiSummary()).isEqualTo("요약 내용");
        }

        @Test
        @DisplayName("이미 시청 세션이 존재하면 VIDEO_ALREADY_WATCHING 예외")
        void alreadyWatching() {
            Long memberId = 1L;
            Long orgId = 10L;
            Long videoId = 100L;

            Member member = mock(Member.class);
            Organization org = mock(Organization.class);
            Video video = mock(Video.class);

            given(memberRepository.findByIdAndOrganizationIdAndStatus(memberId, orgId, ACTIVE))
                    .willReturn(Optional.of(member));

            given(videoRepository.findById(videoId))
                    .willReturn(Optional.of(video));
            given(video.getOrganization()).willReturn(org);
            given(org.getId()).willReturn(orgId);

            given(videoRedisRepository.existsWatchSession(anyString()))
                    .willReturn(true);

            ApiException ex = assertThrows(ApiException.class,
                    () -> videoService.prepareJoinVideoSession(memberId, orgId, videoId));

            assertThat(ex.getResponseStatus()).isEqualTo(VIDEO_ALREADY_WATCHING);
        }

        @Test
        @DisplayName("조직에 속해있지 않으면 MEMBER_NOT_IN_ORGANIZATION 예외")
        void memberNotInOrg() {
            Long memberId = 1L;
            Long orgId = 10L;
            Long videoId = 100L;

            given(memberRepository.findByIdAndOrganizationIdAndStatus(memberId, orgId, ACTIVE))
                    .willReturn(Optional.empty());

            ApiException ex = assertThrows(ApiException.class,
                    () -> videoService.prepareJoinVideoSession(memberId, orgId, videoId));

            assertThat(ex.getResponseStatus()).isEqualTo(MEMBER_NOT_IN_ORGANIZATION);
        }

        @Test
        @DisplayName("영상이 다른 조직에 속해 있으면 VIDEO_NOT_IN_ORGANIZATION 예외")
        void videoNotInOrg() {
            Long memberId = 1L;
            Long orgId = 10L;
            Long videoId = 100L;

            Member member = mock(Member.class);
            Organization org = mock(Organization.class);
            Organization otherOrg = mock(Organization.class);
            Video video = mock(Video.class);

            given(memberRepository.findByIdAndOrganizationIdAndStatus(memberId, orgId, ACTIVE))
                    .willReturn(Optional.of(member));

            given(videoRepository.findById(videoId))
                    .willReturn(Optional.of(video));
            given(video.getOrganization()).willReturn(otherOrg);
            given(otherOrg.getId()).willReturn(999L);

            ApiException ex = assertThrows(ApiException.class,
                    () -> videoService.prepareJoinVideoSession(memberId, orgId, videoId));

            assertThat(ex.getResponseStatus()).isEqualTo(VIDEO_NOT_IN_ORGANIZATION);
        }

        @Test
        @DisplayName("그룹 권한이 없으면 VIDEO_NOT_ACCESSIBLE 예외")
        void notAccessibleToVideo() {
            Long memberId = 1L;
            Long orgId = 10L;
            Long videoId = 100L;

            Member member = mock(Member.class);
            Organization org = mock(Organization.class);
            Video video = mock(Video.class);

            given(memberRepository.findByIdAndOrganizationIdAndStatus(memberId, orgId, ACTIVE))
                    .willReturn(Optional.of(member));

            given(videoRepository.findById(videoId))
                    .willReturn(Optional.of(video));
            given(video.getOrganization()).willReturn(org);
            given(org.getId()).willReturn(orgId);

            given(videoRedisRepository.existsWatchSession(anyString()))
                    .willReturn(false);

            given(memberGroupRepository.isAccessibleToVideo(memberId, videoId))
                    .willReturn(false);

            ApiException ex = assertThrows(ApiException.class,
                    () -> videoService.prepareJoinVideoSession(memberId, orgId, videoId));

            assertThat(ex.getResponseStatus()).isEqualTo(VIDEO_NOT_ACCESSIBLE);
        }
    }

    // ===== openWatchSession =====
    @Nested
    @DisplayName("openWatchSession")
    class OpenWatchSession {

        @Test
        @DisplayName("정상 오픈 시 history 생성/저장, watch 카운트/세션 생성 및 OrgView 증가")
        void success() {
            String sessionId = "session";
            Long memberId = 1L;
            Long orgId = 10L;
            Long videoId = 100L;

            given(videoRedisRepository.existsWatchSession(sessionId))
                    .willReturn(false);

            Video video = mock(Video.class);
            Organization org = mock(Organization.class);

            given(videoRepository.findById(videoId))
                    .willReturn(Optional.of(video));
            given(video.getOrganization()).willReturn(org);
            given(org.getId()).willReturn(orgId);

            given(historyRepository.findByMemberIdAndVideoId(memberId, videoId))
                    .willReturn(Optional.empty());

            Member memberRef = mock(Member.class);
            given(memberRepository.getReferenceById(memberId)).willReturn(memberRef);

            // when
            videoService.openWatchSession(sessionId, memberId, orgId, videoId);

            // then
            verify(video).watch();
            verify(videoRedisRepository).createWatchSession(sessionId, memberId);
            verify(logService).incOrgViewBucket(eq(orgId), any(Instant.class));
        }

        @Test
        @DisplayName("이미 세션 존재하면 VIDEO_ALREADY_WATCHING")
        void alreadyWatching() {
            String sessionId = "session";
            Long memberId = 1L;
            Long orgId = 10L;
            Long videoId = 100L;

            given(videoRedisRepository.existsWatchSession(sessionId))
                    .willReturn(true);

            ApiException ex = assertThrows(ApiException.class,
                    () -> videoService.openWatchSession(sessionId, memberId, orgId, videoId));

            assertThat(ex.getResponseStatus()).isEqualTo(VIDEO_ALREADY_WATCHING);
        }
    }

    // ===== leaveVideoSession =====
    @Nested
    @DisplayName("leaveVideoSession")
    class LeaveVideoSession {

        @Test
        @DisplayName("정상 종료 시 history/seg 로그 업데이트 및 세션 삭제")
        void success() {
            Long memberId = 1L;
            Long orgId = 10L;
            Long videoId = 100L;

            String sessionId = UUID.nameUUIDFromBytes(
                    (memberId.toString() + videoId.toString()).getBytes(StandardCharsets.UTF_8)
            ).toString();

            LeaveVideoSessionInfo info = mock(LeaveVideoSessionInfo.class);
            given(info.getSessionId()).willReturn(sessionId);
            given(info.getOrgId()).willReturn(orgId);
            given(info.getVideoId()).willReturn(videoId);
            given(info.getWatchSegments()).willReturn("1111");
            given(info.getWatchRate()).willReturn(80L);
            given(info.getRecentPosition()).willReturn(40L);
            given(info.getIsQuit()).willReturn(false);

            given(videoRedisRepository.existsWatchSession(sessionId)).willReturn(true);
            given(videoRedisRepository.getMemberIdByWatchSession(sessionId)).willReturn(memberId);

            Member member = mock(Member.class);
            Organization org = mock(Organization.class);
            Video video = mock(Video.class);
            History history = mock(History.class);

            given(memberRepository.findByIdAndStatus(memberId, ACTIVE))
                    .willReturn(Optional.of(member));
            given(member.getOrganization()).willReturn(org);
            given(org.getId()).willReturn(orgId);

            given(videoRepository.findById(videoId))
                    .willReturn(Optional.of(video));
            given(video.getOrganization()).willReturn(org);
            given(video.getWholeTime()).willReturn(40L);

            given(historyRepository.findByMemberIdAndVideoId(memberId, videoId))
                    .willReturn(Optional.of(history));
            given(history.isComplete()).willReturn(false);

            // when
            boolean result = videoService.leaveVideoSession(info);

            // then
            assertThat(result).isTrue();
            verify(videoRedisRepository).deleteWatchSession(sessionId);
            verify(history).update(eq(80L), eq(40L), anyBoolean());
            verify(history).updateLastWatchedAt();
            verify(logService).incSegViewBucket(eq(videoId), any(BigInteger.class), anyInt());
        }

        @Test
        @DisplayName("quit=true 라면 quit 카운트 및 quit 버킷도 증가")
        void success_quit() {
            Long memberId = 1L;
            Long orgId = 10L;
            Long videoId = 100L;

            String sessionId = UUID.nameUUIDFromBytes(
                    (memberId.toString() + videoId.toString()).getBytes(StandardCharsets.UTF_8)
            ).toString();

            LeaveVideoSessionInfo info = mock(LeaveVideoSessionInfo.class);
            given(info.getSessionId()).willReturn(sessionId);
            given(info.getOrgId()).willReturn(orgId);
            given(info.getVideoId()).willReturn(videoId);
            given(info.getWatchSegments()).willReturn("1111");
            given(info.getWatchRate()).willReturn(50L);
            given(info.getRecentPosition()).willReturn(20L);
            given(info.getIsQuit()).willReturn(true);

            given(videoRedisRepository.existsWatchSession(sessionId)).willReturn(true);
            given(videoRedisRepository.getMemberIdByWatchSession(sessionId)).willReturn(memberId);

            Member member = mock(Member.class);
            Organization org = mock(Organization.class);
            Video video = mock(Video.class);
            History history = mock(History.class);

            given(memberRepository.findByIdAndStatus(memberId, ACTIVE))
                    .willReturn(Optional.of(member));
            given(member.getOrganization()).willReturn(org);
            given(org.getId()).willReturn(orgId);

            given(videoRepository.findById(videoId))
                    .willReturn(Optional.of(video));
            given(video.getOrganization()).willReturn(org);
            given(video.getWholeTime()).willReturn(40L);

            given(historyRepository.findByMemberIdAndVideoId(memberId, videoId))
                    .willReturn(Optional.of(history));
            given(history.isComplete()).willReturn(false);

            // when
            boolean result = videoService.leaveVideoSession(info);

            // then
            assertThat(result).isTrue();
            verify(videoRedisRepository).deleteWatchSession(sessionId);
            verify(history).update(eq(50L), eq(20L), anyBoolean());
            verify(history).updateLastWatchedAt();
            verify(logService).incSegViewBucket(eq(videoId), any(BigInteger.class), anyInt());

            verify(logService).incSegQuitBucket(eq(videoId), eq(20L), anyInt());
            verify(video).quit();
        }

        @Test
        @DisplayName("Redis에 세션이 없으면 PLAY_SESSION_NOT_FOUND")
        void noSession() {
            LeaveVideoSessionInfo info = mock(LeaveVideoSessionInfo.class);
            given(info.getSessionId()).willReturn("any-session");

            given(videoRedisRepository.existsWatchSession("any-session"))
                    .willReturn(false);

            ApiException ex = assertThrows(ApiException.class,
                    () -> videoService.leaveVideoSession(info));

            assertThat(ex.getResponseStatus()).isEqualTo(PLAY_SESSION_NOT_FOUND);
        }
    }

    // ===== getMemberVideos =====
    @Nested
    @DisplayName("getMemberVideos")
    class GetMemberVideos {

        @Test
        @DisplayName("멤버가 조직에 속해 있지 않으면 MEMBER_NOT_IN_ORGANIZATION")
        void memberNotInOrg() {
            Long memberId = 1L;
            Long orgId = 10L;

            given(memberRepository.existsByIdAndOrganizationIdAndStatus(memberId, orgId, ACTIVE))
                    .willReturn(false);

            ApiException ex = assertThrows(ApiException.class,
                    () -> videoService.getMemberVideos(memberId, orgId));

            assertThat(ex.getResponseStatus()).isEqualTo(MEMBER_NOT_IN_ORGANIZATION);
        }

        @Test
        @DisplayName("썸네일 키를 CDN URL로 변환해서 반환")
        void success() {
            Long memberId = 1L;
            Long orgId = 10L;

            given(memberRepository.existsByIdAndOrganizationIdAndStatus(memberId, orgId, ACTIVE))
                    .willReturn(true);

            ReadAllVideoItem item1 = new ReadAllVideoItem();
            item1.setThumbnailUrl("thumb1");
            ReadAllVideoItem item2 = new ReadAllVideoItem();
            item2.setThumbnailUrl("thumb2");

            given(videoRepository.findByOrgIdAndCreatorId(orgId, memberId))
                    .willReturn(List.of(item1, item2));
            given(cdnUrlProvider.generateImgUrl("thumb1")).willReturn("cdn/thumb1");
            given(cdnUrlProvider.generateImgUrl("thumb2")).willReturn("cdn/thumb2");

            List<ReadAllVideoItem> result = videoService.getMemberVideos(memberId, orgId);

            assertThat(result)
                    .extracting(ReadAllVideoItem::getThumbnailUrl)
                    .containsExactly("cdn/thumb1", "cdn/thumb2");
        }
    }

    // ===== createVideo =====
    @Nested
    @DisplayName("createVideo")
    class CreateVideo {

        @Test
        @DisplayName("정상 생성 시 Video 저장 및 presigned URL 반환")
        void success() throws Exception {
            Long memberId = 1L;
            Long orgId = 10L;

            Organization org = mock(Organization.class);
            Member member = mock(Member.class);

            given(organizationRepository.findById(orgId)).willReturn(Optional.of(org));
            given(memberRepository.findByIdAndOrganizationIdAndStatus(memberId, orgId, ACTIVE))
                    .willReturn(Optional.of(member));

            CreateVideoRequest req = mock(CreateVideoRequest.class);
            given(req.getAiFunction()).willReturn("SUMMARY");
            given(req.getTitle()).willReturn("title");
            given(req.getDescription()).willReturn("desc");
            given(req.getWholeTime()).willReturn(120L);
            given(req.getIsComment()).willReturn(true);
            given(req.getExpiredAt()).willReturn(LocalDate.now());
            given(req.getMemberGroups()).willReturn(List.of(1L, 2L));
            given(req.getCategories()).willReturn(List.of(10L, 20L));

            MultipartFile thumbnail = mock(MultipartFile.class);
            given(req.getThumbnailImg()).willReturn(thumbnail);
            given(thumbnail.getOriginalFilename()).willReturn("thumb.png");

            given(s3Util.isImageFile(thumbnail)).willReturn(true);
            given(s3Util.generateVideoKey(eq(orgId), anyString()))
                    .willReturn("org-10/uuid/video.mp4");
            given(s3Util.generateImgKey(eq(orgId), anyString(), anyString(), any()))
                    .willReturn("images/org-10/thumb.png");
            given(s3Util.generateHlsPrefix("org-10/uuid/video.mp4"))
                    .willReturn("hls/org-10/uuid");

            given(videoRepository.save(any(Video.class))).willAnswer(invocation -> {
                return invocation.getArgument(0);
            });

            Set<Long> myGroupIds = Set.of(1L, 2L, 3L);
            MemberGroupMapping m1 = mock(MemberGroupMapping.class);
            MemberGroupMapping m2 = mock(MemberGroupMapping.class);
            MemberGroup g1 = mock(MemberGroup.class);
            MemberGroup g2 = mock(MemberGroup.class);
            Organization groupOrg = mock(Organization.class);
            given(groupOrg.getId()).willReturn(orgId);
            given(g1.getId()).willReturn(1L);
            given(g2.getId()).willReturn(2L);
            given(g1.getOrganization()).willReturn(groupOrg);
            given(g2.getOrganization()).willReturn(groupOrg);
            given(m1.getMemberGroup()).willReturn(g1);
            given(m2.getMemberGroup()).willReturn(g2);

            given(memberGroupMappingRepository.findAllByMemberId(memberId))
                    .willReturn(List.of(m1, m2));
            given(memberGroupRepository.findAllById(List.of(1L, 2L)))
                    .willReturn(List.of(g1, g2));

            Category c1 = mock(Category.class);
            Category c2 = mock(Category.class);
            given(c1.getMemberGroupId()).willReturn(1L);
            given(c2.getMemberGroupId()).willReturn(2L);
            given(categoryRepository.findAllById(List.of(10L, 20L)))
                    .willReturn(List.of(c1, c2));

            given(s3Util.generatePresignedUploadUrl("org-10/uuid/video.mp4"))
                    .willReturn(new URL("https://s3/presigned"));

            CreateVideoResponse res = videoService.createVideo(memberId, orgId, req);

            assertThat(res.getPresignedUrl()).isEqualTo("https://s3/presigned");
            verify(videoMemberGroupMappingRepository).saveAll(anyList());
            verify(videoCategoryMappingRepository).saveAll(anyList());
        }
    }

    // ===== updateVideoEncodingResult =====
    @Nested
    @DisplayName("updateVideoEncodingResult")
    class UpdateVideoEncodingResult {

        @Test
        @DisplayName("SUCCESS - AI 기능 수행 후 업로드 상태 COMPLETE로 변경")
        void successWithAi() {
            Long orgId = 10L;
            String uuid = "uuid";
            String videoKey = "org-10/uuid/video.mp4";

            given(s3Util.generateVideoKey(orgId, uuid))
                    .willReturn(videoKey);

            Video video = mock(Video.class);
            Organization org = mock(Organization.class);
            given(org.getId()).willReturn(orgId);

            given(videoRepository.findByVideoKey(videoKey))
                    .willReturn(Optional.of(video));
            given(video.getOrganization()).willReturn(org);
            given(video.getId()).willReturn(100L);
            given(video.getAiFunctionType()).willReturn(SUMMARY);

            SuccessResponse res = videoService.updateVideoEncodingResult(orgId, uuid, "SUCCESS");

            assertThat(res.getIsSuccess()).isTrue();
            verify(aiFunctionService).processAiFunction(100L, videoKey, SUMMARY);
            verify(video).setUploadStatus(COMPLETE);
        }

        @Test
        @DisplayName("FAILED - 매핑 삭제, 파일 삭제, 상태 FAIL")
        void failed() {
            Long orgId = 10L;
            String uuid = "uuid";
            String videoKey = "org-10/uuid/video.mp4";

            given(s3Util.generateVideoKey(orgId, uuid))
                    .willReturn(videoKey);

            Video video = mock(Video.class);
            Organization org = mock(Organization.class);
            given(org.getId()).willReturn(orgId);

            given(videoRepository.findByVideoKey(videoKey))
                    .willReturn(Optional.of(video));
            given(video.getOrganization()).willReturn(org);
            given(video.getId()).willReturn(100L);
            given(video.getVideoKey()).willReturn("videoKey");
            given(video.getThumbnailKey()).willReturn("thumbKey");

            SuccessResponse res = videoService.updateVideoEncodingResult(orgId, uuid, "FAILED");

            assertThat(res.getIsSuccess()).isTrue();
            verify(videoMemberGroupMappingRepository).deleteAllByVideoId(100L);
            verify(videoCategoryMappingRepository).deleteAllByVideoId(100L);
            verify(video).setUploadStatus(FAIL);
            verify(s3Util).deleteFileByKey("videoKey", false);
            verify(s3Util).deleteFileByKey("thumbKey", true);
        }

        @Test
        @DisplayName("알 수 없는 status 면 INVALID_AIRFLOW_STATUS")
        void invalidStatus() {
            Long orgId = 10L;
            String uuid = "uuid";
            String videoKey = "org-10/uuid/video.mp4";

            given(s3Util.generateVideoKey(orgId, uuid))
                    .willReturn(videoKey);

            Video video = mock(Video.class);
            Organization org = mock(Organization.class);
            given(org.getId()).willReturn(orgId);

            given(videoRepository.findByVideoKey(videoKey))
                    .willReturn(Optional.of(video));
            given(video.getOrganization()).willReturn(org);

            ApiException ex = assertThrows(ApiException.class,
                    () -> videoService.updateVideoEncodingResult(orgId, uuid, "UNKNOWN"));

            assertThat(ex.getResponseStatus()).isEqualTo(INVALID_AIRFLOW_STATUS);
        }
    }

    // ===== readVideoEncodingResult =====
    @Nested
    @DisplayName("readVideoEncodingResult")
    class ReadVideoEncodingResult {

        @Test
        @DisplayName("정상 조회 시 UploadStatusType 반환")
        void success() {
            Long memberId = 1L;
            Long orgId = 10L;
            Long videoId = 100L;

            Video video = mock(Video.class);
            Organization org = mock(Organization.class);
            Member creator = mock(Member.class);

            given(videoRepository.findById(videoId))
                    .willReturn(Optional.of(video));
            given(video.getOrganization()).willReturn(org);
            given(org.getId()).willReturn(orgId);
            given(video.getCreator()).willReturn(creator);
            given(creator.getId()).willReturn(memberId);
            given(video.getUploadStatus()).willReturn(COMPLETE);

            UploadStatusType res = videoService.readVideoEncodingResult(memberId, orgId, videoId);

            assertThat(res).isEqualTo(COMPLETE);
        }

        @Test
        @DisplayName("업로드 실패 상태라면 비디오 삭제 후 FAIL 반환")
        void deleteOnFail() {
            Long memberId = 1L;
            Long orgId = 10L;
            Long videoId = 100L;

            Video video = mock(Video.class);
            Organization org = mock(Organization.class);
            Member creator = mock(Member.class);

            given(videoRepository.findById(videoId))
                    .willReturn(Optional.of(video));
            given(video.getOrganization()).willReturn(org);
            given(org.getId()).willReturn(orgId);
            given(video.getCreator()).willReturn(creator);
            given(creator.getId()).willReturn(memberId);
            given(video.getUploadStatus()).willReturn(FAIL);

            UploadStatusType res = videoService.readVideoEncodingResult(memberId, orgId, videoId);

            assertThat(res).isEqualTo(FAIL);
            verify(videoRepository).delete(video);
        }
    }

    // ===== readVideoInfo =====
    @Nested
    @DisplayName("readVideoInfo")
    class ReadVideoInfo {

        @Test
        @DisplayName("내 그룹이 없으면 빈 memberGroupItems 와 openScope 반환")
        void noMyGroups() {
            Long orgId = 10L;
            Long memberId = 1L;
            Long videoId = 100L;

            Video video = mock(Video.class);
            Organization org = mock(Organization.class);
            Member creator = mock(Member.class);

            given(videoRepository.findByIdAndOrganizationId(videoId, orgId))
                    .willReturn(Optional.of(video));
            given(video.getCreator()).willReturn(creator);
            given(creator.getId()).willReturn(memberId);
            given(video.getThumbnailKey()).willReturn("thumb");
            given(cdnUrlProvider.generateImgUrl("thumb")).willReturn("cdn/thumb");
            given(videoMemberGroupMappingRepository.findAllByVideoId(videoId))
                    .willReturn(List.of());
            given(videoCategoryMappingRepository.findAllByVideoId(videoId))
                    .willReturn(List.of());
            given(video.getTitle()).willReturn("title");
            given(video.getDescription()).willReturn("desc");
            given(video.getWatchCnt()).willReturn(10L);
            given(video.getExpiredAt()).willReturn(LocalDate.now());
            given(video.getIsComment()).willReturn(true);

            given(memberGroupMappingRepository.findAllByMemberId(memberId))
                    .willReturn(List.of());

            ReadVideoInfoResponse res = videoService.readVideoInfo(orgId, memberId, videoId);

            assertThat(res.getMemberGroups()).isEmpty();
            assertThat(res.getOpenScope()).isEqualTo(OpenScopeType.PUBLIC);
        }
    }

    // ===== modifyVideo =====
    @Nested
    @DisplayName("modifyVideo")
    class ModifyVideo {

        @Test
        @DisplayName("정상 수정 시 true 반환 및 매핑 재저장")
        void success() {
            Long orgId = 10L;
            Long memberId = 1L;
            Long videoId = 100L;

            ModifyVideoRequest req = mock(ModifyVideoRequest.class);
            given(req.getMemberGroups()).willReturn(List.of(1L, 2L));
            given(req.getCategories()).willReturn(List.of(10L, 20L));
            given(req.getDescription()).willReturn("new desc");
            given(req.getIsComment()).willReturn(false);
            given(req.getExpiredAt()).willReturn(LocalDate.now());

            Video video = mock(Video.class);
            Organization org = mock(Organization.class);
            Member creator = mock(Member.class);

            given(videoRepository.findByIdAndOrganizationId(videoId, orgId))
                    .willReturn(Optional.of(video));
            given(video.getCreator()).willReturn(creator);
            given(creator.getId()).willReturn(memberId);

            MemberGroupMapping m1 = mock(MemberGroupMapping.class);
            MemberGroupMapping m2 = mock(MemberGroupMapping.class);
            MemberGroup g1 = mock(MemberGroup.class);
            MemberGroup g2 = mock(MemberGroup.class);
            Organization groupOrg = mock(Organization.class);
            given(groupOrg.getId()).willReturn(orgId);
            given(g1.getId()).willReturn(1L);
            given(g2.getId()).willReturn(2L);
            given(g1.getOrganization()).willReturn(groupOrg);
            given(g2.getOrganization()).willReturn(groupOrg);
            given(m1.getMemberGroup()).willReturn(g1);
            given(m2.getMemberGroup()).willReturn(g2);

            given(memberGroupMappingRepository.findAllByMemberId(memberId))
                    .willReturn(List.of(m1, m2));
            given(memberGroupRepository.findAllById(List.of(1L, 2L)))
                    .willReturn(List.of(g1, g2));

            Category c1 = mock(Category.class);
            Category c2 = mock(Category.class);
            given(c1.getMemberGroupId()).willReturn(1L);
            given(c2.getMemberGroupId()).willReturn(2L);
            given(categoryRepository.findAllById(List.of(10L, 20L)))
                    .willReturn(List.of(c1, c2));

            boolean res = videoService.modifyVideo(orgId, memberId, videoId, req);

            assertThat(res).isTrue();
            verify(video).modify(eq("new desc"), eq(false), any());
            verify(videoMemberGroupMappingRepository).deleteAllByVideoId(videoId);
            verify(videoCategoryMappingRepository).deleteAllByVideoId(videoId);
            verify(videoMemberGroupMappingRepository).saveAll(anyList());
            verify(videoCategoryMappingRepository).saveAll(anyList());
        }
    }

    // ===== deleteVideo =====
    @Nested
    @DisplayName("deleteVideo")
    class DeleteVideo {

        @Test
        @DisplayName("정상 삭제 시 관련 엔티티 및 S3 파일 모두 정리")
        void success() {
            Long orgId = 10L;
            Long memberId = 1L;
            Long videoId = 100L;

            Video video = mock(Video.class);
            Organization org = mock(Organization.class);
            Member creator = mock(Member.class);

            given(videoRepository.findByIdAndOrganizationId(videoId, orgId))
                    .willReturn(Optional.of(video));
            given(video.getCreator()).willReturn(creator);
            given(creator.getId()).willReturn(memberId);
            given(video.getThumbnailKey()).willReturn("thumbKey");
            given(video.getVideoKey()).willReturn("videoKey");

            boolean result = videoService.deleteVideo(orgId, memberId, videoId);

            assertThat(result).isTrue();
            verify(s3Util).deleteFileByKey("thumbKey", true);
            verify(s3Util).deleteFileByKey("videoKey", false);
            verify(commentRepository).deleteAllByVideoId(videoId);
            verify(historyRepository).deleteAllByVideoId(videoId);
            verify(quizRepository).deleteAllByVideoId(videoId);
            verify(scrapRepository).deleteAllByVideoId(videoId);
            verify(videoMemberGroupMappingRepository).deleteAllByVideoId(videoId);
            verify(videoCategoryMappingRepository).deleteAllByVideoId(videoId);
            verify(videoRepository).delete(video);
        }
    }
}