package app.allstackproject.privideo.controller.video;

import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.INVALID_VIDEO_LEAVE;
import static app.allstackproject.privideo.common.util.BindingResultUtil.getErrorMessage;

import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.common.response.BaseResponse;
import app.allstackproject.privideo.common.response.SuccessResponse;
import app.allstackproject.privideo.dto.video.JoinVideoSessionResponse;
import app.allstackproject.privideo.dto.video.JoinVideoSessionResult;
import app.allstackproject.privideo.dto.video.LeaveVideoSessionInfo;
import app.allstackproject.privideo.dto.video.LeaveVideoSessionRequest;
import app.allstackproject.privideo.service.video.VideoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/{orgId}/video/{videoId}")
public class WatchVideoController {

    private final VideoService videoService;

    @PreAuthorize("hasAuthority('org:granted')")
    @PostMapping("/join")
    public BaseResponse<JoinVideoSessionResponse> joinVideoSession(
            @AuthenticationPrincipal(expression = "memberId") Long memberId, @PathVariable("orgId") Long orgId,
            @PathVariable("videoId") Long videoId) {
        JoinVideoSessionResult result = videoService.joinVideoSession(memberId, orgId, videoId);
        return new BaseResponse<>(JoinVideoSessionResponse.from(result));
    }

    @PreAuthorize("hasAuthority('org:granted')")
    @PostMapping("/leave")
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
