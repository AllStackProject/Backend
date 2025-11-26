package app.allstackproject.privideo.controller.video;

import static app.allstackproject.privideo.common.config.SwaggerConfig.ORG_AUTH_KEY;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.INVALID_VIDEO_LEAVE;
import static app.allstackproject.privideo.common.util.BindingResultUtil.getErrorMessage;

import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.common.response.BaseResponse;
import app.allstackproject.privideo.common.response.SuccessResponse;
import app.allstackproject.privideo.dto.video.CreateVideoRequest;
import app.allstackproject.privideo.dto.video.CreateVideoResponse;
import app.allstackproject.privideo.dto.video.JoinVideoSessionResponse;
import app.allstackproject.privideo.dto.video.JoinVideoSessionResult;
import app.allstackproject.privideo.dto.video.LeaveVideoSessionInfo;
import app.allstackproject.privideo.dto.video.LeaveVideoSessionRequest;
import app.allstackproject.privideo.dto.video.ModifyVideoRequest;
import app.allstackproject.privideo.dto.video.ReadVideoEncodingResultRequest;
import app.allstackproject.privideo.dto.video.ReadVideoEncodingResultResponse;
import app.allstackproject.privideo.service.video.CloudFrontCookieService;
import app.allstackproject.privideo.service.video.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/{orgId}/video")
@Tag(name = "Video", description = "영상 관련 API")
@SecurityRequirement(name = ORG_AUTH_KEY)
public class VideoController {

    private final VideoService videoService;
    private final CloudFrontCookieService cloudFrontCookieService;

    @PostMapping("")
    @PreAuthorize("hasAuthority('org:granted')")
    @Operation(summary = "영상 업로드")
    public BaseResponse<CreateVideoResponse> createVideo(
            @AuthenticationPrincipal(expression = "memberId") Long memberId,
            @PathVariable Long orgId,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("thumbnail_img") MultipartFile thumbnailImg,
            @RequestParam("whole_time") Long wholeTime,
            @RequestParam("is_comment") Boolean isComment,
            @RequestParam("ai_function") String aiFunction,
            @RequestParam(value = "expired_at", required = false) LocalDate expiredAt,
            @RequestParam(value = "member_groups") List<Long> memberGroups,
            @RequestParam(value = "categories") List<Long> categories) {
        CreateVideoRequest createVideoRequest = new CreateVideoRequest(
                title, description, thumbnailImg, wholeTime,
                isComment, aiFunction, expiredAt, memberGroups, categories
        );
        return new BaseResponse<>(videoService.createVideo(memberId, orgId, createVideoRequest));
    }

    @PostMapping("/airflow/status")
    @Operation(summary = "영상 인코딩 성공 여부")
    public BaseResponse<SuccessResponse> readVideoEncodingResult(
            @Valid @RequestBody ReadVideoEncodingResultRequest updateVideoEncodingResultRequest,
            @PathVariable("orgId") Long orgId) {
        return new BaseResponse<>(
                videoService.updateVideoEncodingResult(orgId, updateVideoEncodingResultRequest.getVideoUuid(),
                        updateVideoEncodingResultRequest.getStatus()));
    }

    @GetMapping("/{videoId}/success")
    @PreAuthorize("hasAuthority('org:granted')")
    @Operation(summary = "영상 업로드 성공 여부")
    public BaseResponse<ReadVideoEncodingResultResponse> readVideoEncodingResult(
            @AuthenticationPrincipal(expression = "memberId") Long memberId,
            @PathVariable("orgId") Long orgId,
            @PathVariable("videoId") Long videoId
    ) {
        return new BaseResponse<>(
                ReadVideoEncodingResultResponse.of(videoService.readVideoEncodingResult(memberId, orgId, videoId)));
    }

    @PostMapping("/{videoId}/join")
    @PreAuthorize("hasAuthority('org:granted')")
    @Operation(summary = "영상 시청 세션 시작")
    public BaseResponse<JoinVideoSessionResponse> joinVideoSession(
            @AuthenticationPrincipal(expression = "memberId") Long memberId, @PathVariable("orgId") Long orgId,
            @PathVariable("videoId") Long videoId, HttpServletResponse response) {
        JoinVideoSessionResult result = videoService.joinVideoSession(memberId, orgId, videoId);
        cloudFrontCookieService.addSignedCookies(response, result.getVideo().getHlsPrefix());

        return new BaseResponse<>(JoinVideoSessionResponse.from(result));
    }

    @PostMapping("/{videoId}/leave")
    @Operation(summary = "영상 시청 세션 종료")
    public BaseResponse<SuccessResponse> leaveVideoSession(@PathVariable("orgId") Long orgId,
                                                           @PathVariable("videoId") Long videoId,
                                                           @Valid @RequestBody LeaveVideoSessionRequest leaveVideoSessionRequest,
                                                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ApiException(INVALID_VIDEO_LEAVE, getErrorMessage(bindingResult));
        }

        LeaveVideoSessionInfo leaveVideoSessionInfo = LeaveVideoSessionInfo.create(
                leaveVideoSessionRequest.getMemberId(), orgId, videoId,
                leaveVideoSessionRequest);
        boolean result = videoService.leaveVideoSession(leaveVideoSessionInfo);
        return new BaseResponse<>(SuccessResponse.of(result));
    }

    @PatchMapping("/{videoId}")
    @Operation(summary = "영상 수정")
    public BaseResponse<SuccessResponse> modifyVideo(@AuthenticationPrincipal(expression = "memberId") Long memberId,
                                                     @PathVariable("orgId") Long orgId,
                                                     @PathVariable("videoId") Long videoId,
                                                     @Valid @RequestBody ModifyVideoRequest modifyVideoRequest) {
        return new BaseResponse<>(
                SuccessResponse.of(videoService.modifyVideo(orgId, memberId, videoId, modifyVideoRequest)));
    }

    @DeleteMapping("/{videoId}") // 끗
    @Operation(summary = "업로드한 영상 삭제")
    public BaseResponse<SuccessResponse> deleteVideo(@AuthenticationPrincipal(expression = "memberId") Long memberId,
                                                     @PathVariable("orgId") Long orgId,
                                                     @PathVariable("videoId") Long videoId) {
        return new BaseResponse<>(SuccessResponse.of(videoService.deleteVideo(orgId, memberId, videoId)));
    }
}
