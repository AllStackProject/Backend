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
import app.allstackproject.privideo.service.video.CloudFrontCookieService;
import app.allstackproject.privideo.service.video.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
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
@PreAuthorize("hasAuthority('org:granted')")
@Tag(name = "Video", description = "영상 관련 API")
@SecurityRequirement(name = ORG_AUTH_KEY)
public class VideoController {

    private final VideoService videoService;
    private final CloudFrontCookieService cloudFrontCookieService;

    @PostMapping("")
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
            @RequestParam(value = "expired_at", required = false) LocalDate expiredAt) {
        CreateVideoRequest createVideoRequest = new CreateVideoRequest(
                title, description, thumbnailImg, wholeTime,
                isComment, aiFunction, expiredAt
        );
        return new BaseResponse<>(videoService.createVideo(memberId, orgId, createVideoRequest));
    }

    @PostMapping("/{videoId}/join")
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
    public BaseResponse<SuccessResponse> leaveVideoSession(
            @AuthenticationPrincipal(expression = "memberId") Long memberId, @PathVariable("orgId") Long orgId,
            @PathVariable("videoId") Long videoId,
            @Valid @RequestBody LeaveVideoSessionRequest leaveVideoSessionRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ApiException(INVALID_VIDEO_LEAVE, getErrorMessage(bindingResult));
        }

        LeaveVideoSessionInfo leaveVideoSessionInfo = LeaveVideoSessionInfo.create(memberId, orgId, videoId,
                leaveVideoSessionRequest);
        boolean result = videoService.leaveVideoSession(leaveVideoSessionInfo);
        return new BaseResponse<>(SuccessResponse.of(result));
    }
}
