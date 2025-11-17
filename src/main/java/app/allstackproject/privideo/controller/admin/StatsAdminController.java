package app.allstackproject.privideo.controller.admin;

import static app.allstackproject.privideo.common.config.SwaggerConfig.ORG_AUTH_KEY;

import app.allstackproject.privideo.common.response.BaseResponse;
import app.allstackproject.privideo.dto.admin.ReadAllMemberWatchLogResponse;
import app.allstackproject.privideo.dto.admin.ReadAllVideoWatchLogResponse;
import app.allstackproject.privideo.dto.admin.ReadDayWatchCompleteCntResponse;
import app.allstackproject.privideo.dto.admin.ReadHourWatchCompleteCntResponse;
import app.allstackproject.privideo.dto.admin.ReadMemberWatchLogResponse;
import app.allstackproject.privideo.dto.admin.ReadVideoWatchLogResponse;
import app.allstackproject.privideo.service.admin.StatsAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
        return new BaseResponse<>(ReadAllMemberWatchLogResponse.of(statsAdminService.readAllMemberWatchLog(orgId)));
    }

    @GetMapping("/view/member/{memberId}")
    @Operation(summary = "멤버별 영상 시청 기록 조회")
    public BaseResponse<ReadMemberWatchLogResponse> readMemberWatchLog(@PathVariable Long orgId,
                                                                       @PathVariable Long memberId) {
        return new BaseResponse<>(ReadMemberWatchLogResponse.of(statsAdminService.readMemberWatchLog(orgId, memberId)));
    }

    @GetMapping("/view/videos")
    @Operation(summary = "영상별 시청 기록 목록 조회")
    public BaseResponse<ReadAllVideoWatchLogResponse> readAllVideoWatchLog(@PathVariable Long orgId) {
        return new BaseResponse<>(ReadAllVideoWatchLogResponse.of(statsAdminService.readAllVideoWatchLog(orgId)));
    }

    @GetMapping("/view/video/{videoId}")
    @Operation(summary = "영상별 시청 기록 조회")
    public BaseResponse<ReadVideoWatchLogResponse> readVideoWatchLog(@PathVariable Long orgId,
                                                                     @PathVariable Long videoId) {
        return new BaseResponse<>(ReadVideoWatchLogResponse.of(statsAdminService.readVideoWatchLog(orgId, videoId)));
    }

    @GetMapping("/report/{standardMonth}/day")
    @Operation(summary = "요일별 조회수 조회")
    public BaseResponse<ReadDayWatchCompleteCntResponse> readDayWatchCompleteCnt(
            @AuthenticationPrincipal(expression = "orgId") Long orgId,
            @Parameter(
                    description = "기준 월 (yyyy-MM 형식)",
                    example = "2025-11"
            ) @PathVariable String standardMonth) {
        return new BaseResponse<>(
                ReadDayWatchCompleteCntResponse.of(statsAdminService.readDayWatchCompleteCnt(orgId, standardMonth)));
    }

    @GetMapping("/report/{standardMonth}/hour")
    @Operation(summary = "시간대별 조회수 조회")
    public BaseResponse<ReadHourWatchCompleteCntResponse> readHourWatchCompleteCnt(
            @AuthenticationPrincipal(expression = "orgId") Long orgId,
            @Parameter(
                    description = "기준 월 (yyyy-MM 형식)",
                    example = "2025-11"
            ) @PathVariable String standardMonth) {
        return new BaseResponse<>(
                ReadHourWatchCompleteCntResponse.of(statsAdminService.readHourWatchCompleteCnt(orgId, standardMonth)));
    }
}
