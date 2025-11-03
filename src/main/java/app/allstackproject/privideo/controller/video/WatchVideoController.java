package app.allstackproject.privideo.controller.video;

import app.allstackproject.privideo.common.response.BaseResponse;
import app.allstackproject.privideo.dto.video.JoinVideoSessionResponse;
import app.allstackproject.privideo.dto.video.JoinVideoSessionResult;
import app.allstackproject.privideo.service.video.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
}
