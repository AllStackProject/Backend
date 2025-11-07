package app.allstackproject.privideo.controller.scrap;

import static app.allstackproject.privideo.common.config.SwaggerConfig.ORG_AUTH_KEY;

import app.allstackproject.privideo.common.response.BaseResponse;
import app.allstackproject.privideo.common.response.SuccessResponse;
import app.allstackproject.privideo.service.video.ScrapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/{orgId}/video/{videoId}/scrap")
@PreAuthorize("hasAuthority('org:granted')")
@Tag(name = "Scrap", description = "영상 스크랩 관련 API")
@SecurityRequirement(name = ORG_AUTH_KEY)
public class ScrapController {

    private final ScrapService scrapService;

    @PostMapping("")
    @Operation(summary = "영상 스크랩 등록")
    public BaseResponse<SuccessResponse> addVideoScrap(@AuthenticationPrincipal(expression = "memberId") Long memberId,
                                                       @PathVariable("orgId") Long orgId,
                                                       @PathVariable("videoId") Long videoId) {
        boolean result = scrapService.addVideoScrap(memberId, orgId, videoId);
        return new BaseResponse<>(SuccessResponse.of(result));
    }

    @DeleteMapping("")
    @Operation(summary = "영상 스크랩 취소")
    public BaseResponse<SuccessResponse> deleteVideoScrap(
            @AuthenticationPrincipal(expression = "memberId") Long memberId,
            @PathVariable("orgId") Long orgId,
            @PathVariable("videoId") Long videoId) {
        boolean result = scrapService.deleteVideoScrap(memberId, orgId, videoId);
        return new BaseResponse<>(SuccessResponse.of(result));
    }
}
