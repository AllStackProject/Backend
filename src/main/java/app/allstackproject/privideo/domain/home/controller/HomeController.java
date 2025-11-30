package app.allstackproject.privideo.domain.home.controller;

import static app.allstackproject.privideo.global.config.SwaggerConfig.ORG_AUTH_KEY;

import app.allstackproject.privideo.domain.home.dto.response.HomeVideoItem;
import app.allstackproject.privideo.domain.home.dto.response.ReadHomeResponse;
import app.allstackproject.privideo.domain.home.dto.response.ReadSearchVideoResponse;
import app.allstackproject.privideo.domain.home.service.HomeService;
import app.allstackproject.privideo.domain.notice.dto.ReadAllNoticeItem;
import app.allstackproject.privideo.domain.notice.dto.ReadAllNoticeResponse;
import app.allstackproject.privideo.domain.notice.dto.ReadNoticeResponse;
import app.allstackproject.privideo.global.response.BaseResponse;
import app.allstackproject.privideo.shared.enums.AuthPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/{orgId}/home")
@PreAuthorize("hasAuthority('org:granted')")
@Tag(name = "Home", description = "홈 관련 API")
@SecurityRequirement(name = ORG_AUTH_KEY)
public class HomeController {

    private final HomeService homeService;

    @GetMapping("")
    @Operation(summary = "홈 조회")
    public BaseResponse<ReadHomeResponse> readHome(
            @AuthenticationPrincipal AuthPrincipal me,
            @PathVariable Long orgId,
            @RequestParam String filter) {
        return new BaseResponse<>(homeService.readHome(me.memberId(), orgId, filter));
    }

    @GetMapping("/search")
    @Operation(summary = "영상 검색")
    public BaseResponse<ReadSearchVideoResponse> readSearchVideo(
            @AuthenticationPrincipal AuthPrincipal me,
            @PathVariable Long orgId,
            @RequestParam String keyword) {
        List<HomeVideoItem> homeVideoItems = homeService.readSearchVideo(me.memberId(), orgId, keyword);
        return new BaseResponse<>(ReadSearchVideoResponse.of(homeVideoItems));
    }

    @GetMapping("/notice")
    @Operation(summary = "공지사항 목록 조회")
    public BaseResponse<ReadAllNoticeResponse> readAllNotice(
            @AuthenticationPrincipal AuthPrincipal me,
            @PathVariable Long orgId) {
        List<ReadAllNoticeItem> allNoticeItems = homeService.readAllNotice(orgId, me.memberId());
        return new BaseResponse<>(ReadAllNoticeResponse.of(allNoticeItems));
    }

    @GetMapping("/notice/{noticeId}")
    @Operation(summary = "공지사항 조회")
    public BaseResponse<ReadNoticeResponse> readNotice(
            @AuthenticationPrincipal AuthPrincipal me,
            @PathVariable Long orgId,
            @PathVariable Long noticeId) {
        return new BaseResponse<>(homeService.readNotice(orgId, me.memberId(), noticeId));
    }
}
