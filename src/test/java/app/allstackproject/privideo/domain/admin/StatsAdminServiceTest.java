package app.allstackproject.privideo.domain.admin;

import static app.allstackproject.privideo.domain.video.service.LogService.SEGMENT_SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.allstackproject.privideo.domain.admin.dto.AgeCountDto;
import app.allstackproject.privideo.domain.admin.dto.AllMemberWatchLogItem;
import app.allstackproject.privideo.domain.admin.dto.AllVideoWatchLogItem;
import app.allstackproject.privideo.domain.admin.dto.GenderCountDto;
import app.allstackproject.privideo.domain.admin.dto.GroupWatchCompleteRate;
import app.allstackproject.privideo.domain.admin.dto.MemberAvgWatchRateDto;
import app.allstackproject.privideo.domain.admin.dto.MemberGroupItem;
import app.allstackproject.privideo.domain.admin.dto.MemberWatchLogItem;
import app.allstackproject.privideo.domain.admin.dto.MemberWatchReport;
import app.allstackproject.privideo.domain.admin.dto.MonthlyWatchItem;
import app.allstackproject.privideo.domain.admin.dto.QuitLogItem;
import app.allstackproject.privideo.domain.admin.dto.ReadAllMemberItem;
import app.allstackproject.privideo.domain.admin.dto.ReadAllVideoIntervalLogItem;
import app.allstackproject.privideo.domain.admin.dto.ReadQuitLogResponse;
import app.allstackproject.privideo.domain.admin.dto.VideoIntervalLogItem;
import app.allstackproject.privideo.domain.admin.dto.VideoRankItem;
import app.allstackproject.privideo.domain.admin.dto.VideoWatchLogItem;
import app.allstackproject.privideo.domain.admin.service.StatsAdminService;
import app.allstackproject.privideo.domain.history.repository.HistoryRepository;
import app.allstackproject.privideo.domain.member.enums.AgeType;
import app.allstackproject.privideo.domain.member.repository.MemberRepository;
import app.allstackproject.privideo.domain.organization.entity.OrgViewLog;
import app.allstackproject.privideo.domain.user.dto.enums.GenderType;
import app.allstackproject.privideo.domain.video.entity.Video;
import app.allstackproject.privideo.domain.video.repository.VideoRepository;
import app.allstackproject.privideo.domain.video.service.LogService;
import app.allstackproject.privideo.global.exception.ApiException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

@ExtendWith(MockitoExtension.class)
class StatsAdminServiceTest {

    @Mock
    MemberRepository memberRepository;
    @Mock
    HistoryRepository historyRepository;
    @Mock
    VideoRepository videoRepository;
    @Mock
    MongoTemplate mongoTemplate;
    @Mock
    LogService logService;

    @InjectMocks
    StatsAdminService statsAdminService;

    @Test
    @DisplayName("readAllMemberWatchLog - 그룹 이름과 평균 시청률 매핑")
    void readAllMemberWatchLog_success() {
        Long orgId = 1L;

        // ReadAllMemberItem 은 서비스 내부에서 getId, getNickname, getMemberGroups 사용 → mock으로 처리
        ReadAllMemberItem m1 = mock(ReadAllMemberItem.class);
        ReadAllMemberItem m2 = mock(ReadAllMemberItem.class);

        when(m1.getId()).thenReturn(1L);
        when(m1.getNickname()).thenReturn("user1");
        when(m1.getMemberGroups()).thenReturn(
                List.of(new MemberGroupItem(1L, "개발팀"))
        );

        when(m2.getId()).thenReturn(2L);
        when(m2.getNickname()).thenReturn("user2");
        when(m2.getMemberGroups()).thenReturn(null); // 그룹 없음

        when(memberRepository.findByOrganizationId(orgId))
                .thenReturn(List.of(m1, m2));

        // 평균 시청률 DTO 도 mock 으로
        MemberAvgWatchRateDto avg1 = mock(MemberAvgWatchRateDto.class);
        when(avg1.getMemberId()).thenReturn(1L);
        when(avg1.getAvgWatchRate()).thenReturn(80L);

        when(historyRepository.findMemberAvgWatchRateByOrgId(orgId))
                .thenReturn(List.of(avg1));

        // when
        List<AllMemberWatchLogItem> result = statsAdminService.readAllMemberWatchLog(orgId);

        // then
        assertEquals(2, result.size());

        AllMemberWatchLogItem r1 = result.get(0);
        assertEquals(1L, r1.getId());
        assertEquals("user1", r1.getNickname());
        assertEquals(List.of("개발팀"), r1.getGroups());
        assertEquals(80L, r1.getAvgWatchRate());

        AllMemberWatchLogItem r2 = result.get(1);
        assertEquals(2L, r2.getId());
        assertEquals("user2", r2.getNickname());
        assertEquals(List.of(), r2.getGroups());
        assertEquals(0L, r2.getAvgWatchRate());
    }

    @Test
    @DisplayName("readMemberWatchLog - 조직에 속하지 않으면 예외")
    void readMemberWatchLog_notInOrg() {
        Long orgId = 1L;
        Long memberId = 10L;

        when(memberRepository.existsByIdAndOrganizationIdAndStatus(eq(memberId), eq(orgId), any()))
                .thenReturn(false);

        assertThrows(ApiException.class,
                () -> statsAdminService.readMemberWatchLog(orgId, memberId));
    }

    @Test
    @DisplayName("readMemberWatchLog - 정상 조회")
    void readMemberWatchLog_success() {
        Long orgId = 1L;
        Long memberId = 10L;

        when(memberRepository.existsByIdAndOrganizationIdAndStatus(eq(memberId), eq(orgId), any()))
                .thenReturn(true);

        MemberWatchLogItem item = mock(MemberWatchLogItem.class);
        when(historyRepository.findWatchLogByMemberId(memberId))
                .thenReturn(List.of(item));

        List<MemberWatchLogItem> result = statsAdminService.readMemberWatchLog(orgId, memberId);

        assertEquals(1, result.size());
        verify(historyRepository).findWatchLogByMemberId(memberId);
    }

    @Test
    @DisplayName("readMemberWatchReport - 조직에 속하지 않으면 예외")
    void readMemberWatchReport_notInOrg() {
        Long orgId = 1L;
        Long memberId = 10L;

        when(memberRepository.existsByIdAndOrganizationIdAndStatus(eq(memberId), eq(orgId), any()))
                .thenReturn(false);

        assertThrows(ApiException.class,
                () -> statsAdminService.readMemberWatchReport(orgId, memberId));
    }

    @Test
    @DisplayName("readMemberWatchReport - 최근 3개월 통계 조회")
    void readMemberWatchReport_success() {
        Long orgId = 1L;
        Long memberId = 10L;

        when(memberRepository.existsByIdAndOrganizationIdAndStatus(eq(memberId), eq(orgId), any()))
                .thenReturn(true);

        when(historyRepository.countByMemberIdAndIsCompleteIsTrueAndCompletedAtBetween(
                eq(memberId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(3L);

        when(historyRepository.findTopCategoriesByMemberIdWithinPeriod(
                eq(memberId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of("백엔드", "프론트엔드"));

        MonthlyWatchItem m1 = mock(MonthlyWatchItem.class);
        MonthlyWatchItem m2 = mock(MonthlyWatchItem.class);

        when(historyRepository.findMonthlyStatsByMemberIdWithinPeriod(
                eq(memberId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(m1, m2));

        MemberWatchReport report = statsAdminService.readMemberWatchReport(orgId, memberId);

        assertEquals(3L, report.getTotalWatchedVideoCnt());
        assertEquals(List.of("백엔드", "프론트엔드"), report.getMostWatchedCategories());
        assertEquals(2, report.getMonthlyWatchedCnts().size());
    }

    @Test
    @DisplayName("readAllVideoWatchLog - 히스토리에서 그대로 반환")
    void readAllVideoWatchLog_success() {
        Long orgId = 1L;
        AllVideoWatchLogItem item = mock(AllVideoWatchLogItem.class);
        when(historyRepository.findAllVideoWatchLogByOrgId(orgId))
                .thenReturn(List.of(item));

        List<AllVideoWatchLogItem> result = statsAdminService.readAllVideoWatchLog(orgId);

        assertEquals(1, result.size());
        assertSame(item, result.get(0));
    }

    @Test
    @DisplayName("readVideoWatchLog - 영상이 조직에 없으면 예외")
    void readVideoWatchLog_notInOrg() {
        Long orgId = 1L;
        Long videoId = 20L;

        when(videoRepository.existsByIdAndOrganizationId(videoId, orgId))
                .thenReturn(false);

        assertThrows(ApiException.class,
                () -> statsAdminService.readVideoWatchLog(orgId, videoId));
    }

    @Test
    @DisplayName("readVideoWatchLog - 정상 조회")
    void readVideoWatchLog_success() {
        Long orgId = 1L;
        Long videoId = 20L;

        when(videoRepository.existsByIdAndOrganizationId(videoId, orgId))
                .thenReturn(true);

        VideoWatchLogItem item = mock(VideoWatchLogItem.class);
        when(historyRepository.findVideoWatchLogByVideoId(videoId))
                .thenReturn(List.of(item));

        List<VideoWatchLogItem> result = statsAdminService.readVideoWatchLog(orgId, videoId);

        assertEquals(1, result.size());
        assertSame(item, result.get(0));
    }

    @Test
    @DisplayName("readDayWatchCompleteCnt - 요일별 합계 계산")
    void readDayWatchCompleteCnt_success() {
        Long orgId = 1L;
        String standardMonth = "2025-01";

        OrgViewLog log = mock(OrgViewLog.class);
        // MONDAY
        when(log.getDate()).thenReturn(LocalDateTime.of(2025, 1, 6, 10, 0));
        when(log.getBuckets()).thenReturn(Map.of(
                "00-03", 1,
                "03-06", 2
        ));

        when(mongoTemplate.find(any(Query.class), eq(OrgViewLog.class)))
                .thenReturn(List.of(log));

        List<Long> result = statsAdminService.readDayWatchCompleteCnt(orgId, standardMonth);

        assertEquals(7, result.size());
        // 월요일 위치(첫번째) 값 = 3
        assertEquals(3L, result.get(0));
        // 나머지 요일은 0
        result.subList(1, 7).forEach(v -> assertEquals(0L, v));
    }

    @Test
    @DisplayName("readHourWatchCompleteCnt - 시간대별 합계 계산")
    void readHourWatchCompleteCnt_success() {
        Long orgId = 1L;
        String standardMonth = "2025-01";

        OrgViewLog log = mock(OrgViewLog.class);
        when(log.getBuckets()).thenReturn(Map.of(
                "00-03", 1,
                "09-12", 5
        ));

        when(mongoTemplate.find(any(Query.class), eq(OrgViewLog.class)))
                .thenReturn(List.of(log));

        List<Long> result = statsAdminService.readHourWatchCompleteCnt(orgId, standardMonth);

        assertEquals(8, result.size());
        assertEquals(1L, result.get(0)); // 00-03
        assertEquals(5L, result.get(3)); // 09-12
    }

    @Test
    @DisplayName("readGroupWatchCompleteLog - Repository 결과 그대로 반환")
    void readGroupWatchCompleteLog_success() {
        Long orgId = 1L;
        String standardMonth = "2025-01";

        GroupWatchCompleteRate item = mock(GroupWatchCompleteRate.class);
        when(historyRepository.findGroupAvgWatchRateByOrgIdWithinPeriod(
                eq(orgId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(item));

        List<GroupWatchCompleteRate> result =
                statsAdminService.readGroupWatchCompleteLog(orgId, standardMonth);

        assertEquals(1, result.size());
        assertSame(item, result.get(0));
    }

    @Test
    @DisplayName("readOrgGenderReport - 성별별 인원수 리포트")
    void readOrgGenderReport_success() {
        Long orgId = 1L;
        GenderCountDto dto = mock(GenderCountDto.class);
        when(dto.getGender()).thenReturn(GenderType.MALE);
        when(dto.getCount()).thenReturn(3L);

        when(memberRepository.countMemberByGender(orgId))
                .thenReturn(List.of(dto));

        Map<GenderType, Long> result = statsAdminService.readOrgGenderReport(orgId);

        assertEquals(1, result.size());
        assertEquals(3L, result.get(GenderType.MALE));
    }

    @Test
    @DisplayName("readOrgAgeReport - 연령대별 인원수 리포트")
    void readOrgAgeReport_success() {
        Long orgId = 1L;
        AgeCountDto dto = mock(AgeCountDto.class);
        when(dto.getAge()).thenReturn(AgeType.TWENTY);
        when(dto.getCount()).thenReturn(5L);

        when(memberRepository.countMemberByAge(orgId))
                .thenReturn(List.of(dto));

        Map<AgeType, Long> result = statsAdminService.readOrgAgeReport(orgId);

        assertEquals(1, result.size());
        assertEquals(5L, result.get(AgeType.TWENTY));
    }

    @Test
    @DisplayName("readAllVideoIntervalLog - Repository 결과 그대로 반환")
    void readAllVideoIntervalLog_success() {
        Long orgId = 1L;
        ReadAllVideoIntervalLogItem item = mock(ReadAllVideoIntervalLogItem.class);
        when(videoRepository.findAllVideoIntervalLogByOrgId(orgId))
                .thenReturn(List.of(item));

        List<ReadAllVideoIntervalLogItem> result =
                statsAdminService.readAllVideoIntervalLog(orgId);

        assertEquals(1, result.size());
        assertSame(item, result.get(0));
    }

    @Test
    @DisplayName("readVideoIntervalLog - 영상이 없으면 예외")
    void readVideoIntervalLog_videoNotFound() {
        Long videoId = 10L;
        when(videoRepository.findById(videoId))
                .thenReturn(java.util.Optional.empty());

        assertThrows(ApiException.class,
                () -> statsAdminService.readVideoIntervalLog(videoId));
    }

    @Test
    @DisplayName("readVideoIntervalLog - 세그먼트별 조회/이탈/비율 계산")
    void readVideoIntervalLog_success() {
        Long videoId = 10L;
        Video video = mock(Video.class);

        when(videoRepository.findById(videoId))
                .thenReturn(java.util.Optional.of(video));
        // 총 3 segment 로 가정
        when(video.getWholeTime()).thenReturn(SEGMENT_SECONDS * 3L);

        when(logService.getSegViewCounts(eq(videoId), anyInt()))
                .thenReturn(List.of(10L, 20L, 30L));
        when(logService.getSegQuitCounts(eq(videoId), anyInt()))
                .thenReturn(List.of(1L, 2L, 3L));

        List<VideoIntervalLogItem> intervals = statsAdminService.readVideoIntervalLog(videoId);

        assertEquals(3, intervals.size());
        VideoIntervalLogItem first = intervals.get(0);
        assertEquals(0L, first.getSegIdx());
        assertEquals(10L, first.getWatchCnt());
        assertEquals(1L, first.getQuitCnt());
    }

    @Test
    @DisplayName("readQuitLog - 상위/하위 이탈률 영상 리포트")
    void readQuitLog_success() {
        Long orgId = 1L;

        QuitLogItem high = mock(QuitLogItem.class);
        QuitLogItem low = mock(QuitLogItem.class);

        when(videoRepository.findTopQuitRateVideosByOrgId(orgId, 3))
                .thenReturn(List.of(high));
        when(videoRepository.findLowQuitRateVideosByOrgId(orgId, 3))
                .thenReturn(List.of(low));

        ReadQuitLogResponse response = statsAdminService.readQuitLog(orgId);

        assertEquals(1, response.getHighQuitRateLogs().size());
        assertEquals(1, response.getLowQuitRateLogs().size());
    }

    @Test
    @DisplayName("readVideoRank - 상위 5개 영상 랭킹 조회")
    void readVideoRank_success() {
        Long orgId = 1L;
        VideoRankItem item = mock(VideoRankItem.class);

        when(videoRepository.findTop5VideoRankByOrgId(orgId))
                .thenReturn(List.of(item));

        List<VideoRankItem> result = statsAdminService.readVideoRank(orgId);

        assertEquals(1, result.size());
        assertSame(item, result.get(0));
    }
}