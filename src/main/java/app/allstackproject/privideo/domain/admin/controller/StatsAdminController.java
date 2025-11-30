package app.allstackproject.privideo.domain.admin.controller;

import static app.allstackproject.privideo.global.config.SwaggerConfig.ORG_AUTH_KEY;

import app.allstackproject.privideo.domain.admin.dto.AllMemberWatchLogItem;
import app.allstackproject.privideo.domain.admin.dto.AllVideoWatchLogItem;
import app.allstackproject.privideo.domain.admin.dto.GroupWatchCompleteRate;
import app.allstackproject.privideo.domain.admin.dto.MemberWatchLogItem;
import app.allstackproject.privideo.domain.admin.dto.MemberWatchReport;
import app.allstackproject.privideo.domain.admin.dto.ReadAllMemberWatchLogResponse;
import app.allstackproject.privideo.domain.admin.dto.ReadAllVideoIntervalLogItem;
import app.allstackproject.privideo.domain.admin.dto.ReadAllVideoIntervalLogResponse;
import app.allstackproject.privideo.domain.admin.dto.ReadAllVideoWatchLogResponse;
import app.allstackproject.privideo.domain.admin.dto.ReadDayWatchCompleteCntResponse;
import app.allstackproject.privideo.domain.admin.dto.ReadGroupWatchCompleteLogResponse;
import app.allstackproject.privideo.domain.admin.dto.ReadHourWatchCompleteCntResponse;
import app.allstackproject.privideo.domain.admin.dto.ReadMemberWatchLogResponse;
import app.allstackproject.privideo.domain.admin.dto.ReadMemberWatchReportResponse;
import app.allstackproject.privideo.domain.admin.dto.ReadOrgAgeReportResponse;
import app.allstackproject.privideo.domain.admin.dto.ReadOrgGenderReportResponse;
import app.allstackproject.privideo.domain.admin.dto.ReadQuitLogResponse;
import app.allstackproject.privideo.domain.admin.dto.ReadVideoIntervalLogResponse;
import app.allstackproject.privideo.domain.admin.dto.ReadVideoRankResponse;
import app.allstackproject.privideo.domain.admin.dto.ReadVideoWatchLogResponse;
import app.allstackproject.privideo.domain.admin.dto.VideoIntervalLogItem;
import app.allstackproject.privideo.domain.admin.dto.VideoRankItem;
import app.allstackproject.privideo.domain.admin.dto.VideoWatchLogItem;
import app.allstackproject.privideo.domain.admin.service.StatsAdminService;
import app.allstackproject.privideo.domain.member.enums.AgeType;
import app.allstackproject.privideo.domain.user.dto.enums.GenderType;
import app.allstackproject.privideo.global.response.BaseResponse;
import app.allstackproject.privideo.shared.enums.AuthPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/org/{orgId}")
@PreAuthorize("hasAuthority('org:granted') and hasAuthority('perm:stats_report')")
@Tag(name = "Admin-Stats", description = "관리자 통계/리포트 API")
@SecurityRequirement(name = ORG_AUTH_KEY)
public class StatsAdminController {

    private final StatsAdminService statsAdminService;

    @GetMapping("/view/members")
    @Operation(summary = "멤버별 영상 시청 기록 목록 조회")
    public BaseResponse<ReadAllMemberWatchLogResponse> readAllMemberWatchLog(@PathVariable Long orgId) {
        List<AllMemberWatchLogItem> allMemberWatchLogItems = statsAdminService.readAllMemberWatchLog(orgId);
        return new BaseResponse<>(ReadAllMemberWatchLogResponse.of(allMemberWatchLogItems));
    }

    @GetMapping("/view/member/{memberId}")
    @Operation(summary = "멤버별 영상 시청 기록 조회")
    public BaseResponse<ReadMemberWatchLogResponse> readMemberWatchLog(
            @PathVariable Long orgId,
            @PathVariable Long memberId) {
        List<MemberWatchLogItem> memberWatchLogItems = statsAdminService.readMemberWatchLog(orgId, memberId);
        return new BaseResponse<>(ReadMemberWatchLogResponse.of(memberWatchLogItems));
    }

    @GetMapping("/view/report/member/{memberId}")
    @Operation(summary = "멤버별 영상 리포트 조회")
    public BaseResponse<ReadMemberWatchReportResponse> readMemberWatchReport(
            @PathVariable Long orgId,
            @PathVariable Long memberId) {
        MemberWatchReport memberWatchReport = statsAdminService.readMemberWatchReport(orgId, memberId);
        return new BaseResponse<>(ReadMemberWatchReportResponse.of(memberWatchReport));
    }

    @GetMapping("/view/videos")
    @Operation(summary = "영상별 시청 기록 목록 조회")
    public BaseResponse<ReadAllVideoWatchLogResponse> readAllVideoWatchLog(@PathVariable Long orgId) {
        List<AllVideoWatchLogItem> allVideoWatchLogItems = statsAdminService.readAllVideoWatchLog(orgId);
        return new BaseResponse<>(ReadAllVideoWatchLogResponse.of(allVideoWatchLogItems));
    }

    @GetMapping("/view/video/{videoId}")
    @Operation(summary = "영상별 시청 기록 조회")
    public BaseResponse<ReadVideoWatchLogResponse> readVideoWatchLog(
            @PathVariable Long orgId,
            @PathVariable Long videoId) {
        List<VideoWatchLogItem> videoWatchLogItems = statsAdminService.readVideoWatchLog(orgId, videoId);
        return new BaseResponse<>(ReadVideoWatchLogResponse.of(videoWatchLogItems));
    }

    @GetMapping("/report/day/{standardMonth}")
    @Operation(summary = "요일별 조회수 조회")
    public BaseResponse<ReadDayWatchCompleteCntResponse> readDayWatchCompleteCnt(
            @AuthenticationPrincipal AuthPrincipal me,
            @Parameter(
                    description = "기준 월 (yyyy-MM 형식)",
                    example = "2025-11"
            ) @PathVariable String standardMonth) {
        List<Long> dayWatchCompleteCnts = statsAdminService.readDayWatchCompleteCnt(me.orgId(), standardMonth);
        return new BaseResponse<>(ReadDayWatchCompleteCntResponse.of(dayWatchCompleteCnts));
    }

    @GetMapping("/report/hour/{standardMonth}")
    @Operation(summary = "시간대별 조회수 조회")
    public BaseResponse<ReadHourWatchCompleteCntResponse> readHourWatchCompleteCnt(
            @AuthenticationPrincipal AuthPrincipal me,
            @Parameter(
                    description = "기준 월 (yyyy-MM 형식)",
                    example = "2025-11"
            ) @PathVariable String standardMonth) {
        List<Long> watchHourCompleteCnts = statsAdminService.readHourWatchCompleteCnt(me.orgId(), standardMonth);
        return new BaseResponse<>(ReadHourWatchCompleteCntResponse.of(watchHourCompleteCnts));
    }

    @GetMapping("/report/watchRate/{standardMonth}")
    @Operation(summary = "그룹별 시청 완료율 조회")
    public BaseResponse<ReadGroupWatchCompleteLogResponse> readGroupWatchCompleteLog(
            @AuthenticationPrincipal AuthPrincipal me,
            @Parameter(
                    description = "기준 월 (yyyy-MM 형식)",
                    example = "2025-11"
            ) @PathVariable String standardMonth) {
        List<GroupWatchCompleteRate> groupWatchCompleteRates = statsAdminService.readGroupWatchCompleteLog(me.orgId(),
                standardMonth);
        return new BaseResponse<>(ReadGroupWatchCompleteLogResponse.of(groupWatchCompleteRates));
    }

    @GetMapping("/report/gender")
    @Operation(summary = "조직 내 성별 분포 조회")
    public BaseResponse<ReadOrgGenderReportResponse> readOrgGenderReport(@PathVariable Long orgId) {
        Map<GenderType, Long> genderReport = statsAdminService.readOrgGenderReport(orgId);
        return new BaseResponse<>(ReadOrgGenderReportResponse.of(genderReport));
    }

    @GetMapping("/report/age")
    @Operation(summary = "조직 내 연령대 분포 조회")
    public BaseResponse<ReadOrgAgeReportResponse> readOrgAgeReport(@PathVariable Long orgId) {
        Map<AgeType, Long> ageReport = statsAdminService.readOrgAgeReport(orgId);
        return new BaseResponse<>(ReadOrgAgeReportResponse.of(ageReport));
    }

    @GetMapping("/report/interval")
    @Operation(summary = "영상 시청 구간 분석 목록 조회")
    public BaseResponse<ReadAllVideoIntervalLogResponse> readAllVideoIntervalLog(@PathVariable Long orgId) {
        List<ReadAllVideoIntervalLogItem> allVideoIntervalLogItems = statsAdminService.readAllVideoIntervalLog(orgId);
        return new BaseResponse<>(ReadAllVideoIntervalLogResponse.of(allVideoIntervalLogItems));
    }

    @GetMapping("/report/interval/{videoId}")
    @Operation(summary = "영상 시청 구간 분석 조회", description = "세그먼트 인덱스는 영상의 뒷 구간부터 시작합니다.")
    public BaseResponse<ReadVideoIntervalLogResponse> readVideoIntervalLog(@PathVariable Long videoId) {
        List<VideoIntervalLogItem> videoIntervalLogItems = statsAdminService.readVideoIntervalLog(videoId);
        return new BaseResponse<>(ReadVideoIntervalLogResponse.of(videoIntervalLogItems));
    }

    @GetMapping("/report/quit")
    @Operation(summary = "중도 이탈 분석 조회")
    public BaseResponse<ReadQuitLogResponse> readQuitLog(@PathVariable Long orgId) {
        return new BaseResponse<>(statsAdminService.readQuitLog(orgId));
    }

    @GetMapping("/report/top-rank")
    @Operation(summary = "인기 동영상 목록 조회")
    public BaseResponse<ReadVideoRankResponse> readVideoRank(@PathVariable Long orgId) {
        List<VideoRankItem> videoRankItems = statsAdminService.readVideoRank(orgId);
        return new BaseResponse<>(ReadVideoRankResponse.of(videoRankItems));
    }
}
