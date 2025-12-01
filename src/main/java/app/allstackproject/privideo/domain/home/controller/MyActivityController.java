package app.allstackproject.privideo.domain.home.controller;

import static app.allstackproject.privideo.global.config.SwaggerConfig.ORG_AUTH_KEY;

import app.allstackproject.privideo.domain.admin.dto.ReadAllMemberGroupItem;
import app.allstackproject.privideo.domain.admin.dto.ReadAllVideosResponse;
import app.allstackproject.privideo.domain.admin.dto.ReadVideoIntervalLogResponse;
import app.allstackproject.privideo.domain.admin.dto.VideoIntervalLogItem;
import app.allstackproject.privideo.domain.comment.dto.response.CommentResponse;
import app.allstackproject.privideo.domain.comment.service.CommentService;
import app.allstackproject.privideo.domain.history.dto.HistoryResponse;
import app.allstackproject.privideo.domain.history.service.HistoryService;
import app.allstackproject.privideo.domain.home.dto.request.ModifyNicknameRequest;
import app.allstackproject.privideo.domain.member.dto.response.ReadMemberGroupResponse;
import app.allstackproject.privideo.domain.organization.dto.response.ReadMemberOrganizationInfoResponse;
import app.allstackproject.privideo.domain.organization.service.OrganizationService;
import app.allstackproject.privideo.domain.scrap.dto.ScrapResponse;
import app.allstackproject.privideo.domain.scrap.service.ScrapService;
import app.allstackproject.privideo.domain.video.service.VideoService;
import app.allstackproject.privideo.global.response.BaseResponse;
import app.allstackproject.privideo.global.response.SuccessResponse;
import app.allstackproject.privideo.shared.enums.AuthPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("{orgId}/myactivity")
@Slf4j
@PreAuthorize("hasAuthority('org:granted')")
@Tag(name = "MyActivity", description = "내 활동 관련 API")
@SecurityRequirement(name = ORG_AUTH_KEY)
public class MyActivityController {

    private final HistoryService historyService;
    private final ScrapService scrapService;
    private final CommentService commentService;
    private final OrganizationService organizationService;
    private final VideoService videoService;

    @GetMapping("/myvideo")
    @Operation(summary = "업로드한 영상 목록 조회")
    public BaseResponse<ReadAllVideosResponse> readMyVideos(
            @AuthenticationPrincipal AuthPrincipal me,
            @PathVariable Long orgId) {
        return new BaseResponse<>(ReadAllVideosResponse.of(videoService.getMemberVideos(me.memberId(), orgId)));
    }

    @GetMapping("/myvideo/{videoId}")
    @Operation(summary = "업로드한 영상 통계 조회")
    public BaseResponse<ReadVideoIntervalLogResponse> readMyVideoReport(
            @AuthenticationPrincipal AuthPrincipal me,
            @PathVariable Long orgId,
            @PathVariable Long videoId) {
        List<VideoIntervalLogItem> videoIntervalLogItems = historyService.readMyVideoReport(me.memberId(), orgId,
                videoId);
        return new BaseResponse<>(ReadVideoIntervalLogResponse.of(videoIntervalLogItems));
    }

    @GetMapping("/video")
    @Operation(summary = "영상 시청 기록 조회")
    public BaseResponse<HistoryResponse> getVideoHistory(
            @AuthenticationPrincipal AuthPrincipal me,
            @PathVariable long orgId) {
        return new BaseResponse<>(historyService.getUserVideos(me.memberId(), orgId));
    }

    @GetMapping("/scrap")
    @Operation(summary = "스크랩한 영상 조회")
    public BaseResponse<ScrapResponse> getUserScrabs(
            @AuthenticationPrincipal AuthPrincipal me,
            @PathVariable long orgId) {
        return new BaseResponse<>(scrapService.getUserScraps(me.memberId(), orgId));
    }

    @GetMapping("/comment")
    @Operation(summary = "작성한 댓글 조회")
    public BaseResponse<CommentResponse> getUserComments(
            @AuthenticationPrincipal AuthPrincipal me,
            @PathVariable long orgId) {
        return new BaseResponse<>(commentService.getUserComments(me.memberId(), orgId));
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "작성한 댓글 삭제")
    public BaseResponse<SuccessResponse> deleteComment(
            @AuthenticationPrincipal AuthPrincipal me,
            @PathVariable Long orgId,
            @PathVariable Long commentId) {
        return new BaseResponse<>(SuccessResponse.of(commentService.deleteComment(me.memberId(), orgId, commentId)));
    }

    @GetMapping("/info")
    @Operation(summary = "조직 정보 조회")
    public BaseResponse<ReadMemberOrganizationInfoResponse> readOrganizationInfo(
            @AuthenticationPrincipal AuthPrincipal me,
            @PathVariable Long orgId) {
        return new BaseResponse<>(organizationService.readOrganizationInfo(me.memberId(), orgId));
    }

    @GetMapping("/group")
    @Operation(summary = "내 그룹 목록 조회")
    public BaseResponse<ReadMemberGroupResponse> readMemberGroup(
            @AuthenticationPrincipal AuthPrincipal me,
            @PathVariable Long orgId) {
        List<ReadAllMemberGroupItem> memberGroupItems = organizationService.readMemberGroup(me.memberId(), orgId);
        return new BaseResponse<>(ReadMemberGroupResponse.of(memberGroupItems));
    }

    @PutMapping("/nickname")
    @Operation(summary = "닉네임 변경")
    public BaseResponse<SuccessResponse> modifyNickname(
            @AuthenticationPrincipal AuthPrincipal me,
            @PathVariable Long orgId,
            @Valid @RequestBody ModifyNicknameRequest modifyNicknameRequest) {
        return new BaseResponse<>(SuccessResponse.of(
                organizationService.modifyNickname(me.memberId(), orgId, modifyNicknameRequest.getNickname())));
    }
}

