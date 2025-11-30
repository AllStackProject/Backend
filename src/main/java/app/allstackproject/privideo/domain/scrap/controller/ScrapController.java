package app.allstackproject.privideo.domain.scrap.controller;

import static app.allstackproject.privideo.global.config.SwaggerConfig.ORG_AUTH_KEY;

import app.allstackproject.privideo.domain.scrap.service.ScrapService;
import app.allstackproject.privideo.global.response.BaseResponse;
import app.allstackproject.privideo.global.response.SuccessResponse;
import app.allstackproject.privideo.shared.enums.AuthPrincipal;
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
    public BaseResponse<SuccessResponse> addVideoScrap(
            @AuthenticationPrincipal AuthPrincipal me,
            @PathVariable Long orgId,
            @PathVariable Long videoId) {
        return new BaseResponse<>(SuccessResponse.of(scrapService.addVideoScrap(me.memberId(), orgId, videoId)));
    }

    @DeleteMapping("")
    @Operation(summary = "영상 스크랩 취소")
    public BaseResponse<SuccessResponse> deleteVideoScrap(
            @AuthenticationPrincipal AuthPrincipal me,
            @PathVariable Long orgId,
            @PathVariable Long videoId) {
        return new BaseResponse<>(SuccessResponse.of(scrapService.deleteVideoScrap(me.memberId(), orgId, videoId)));
    }
}
