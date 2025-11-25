package app.allstackproject.privideo.controller.admin;

import static app.allstackproject.privideo.common.config.SwaggerConfig.ORG_AUTH_KEY;

import app.allstackproject.privideo.common.response.BaseResponse;
import app.allstackproject.privideo.common.response.SuccessResponse;
import app.allstackproject.privideo.dto.admin.AdminReadAllNoticeResponse;
import app.allstackproject.privideo.dto.admin.AdminReadNoticeResponse;
import app.allstackproject.privideo.dto.admin.CreateNoticeRequest;
import app.allstackproject.privideo.service.admin.NoticeAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/org/{orgId}")
@PreAuthorize("hasAuthority('org:granted') and hasAuthority('perm:notice')")
@Tag(name = "Admin-Notice", description = "관리자 공지 API")
@SecurityRequirement(name = ORG_AUTH_KEY)
public class NoticeAdminController {

    private final NoticeAdminService noticeAdminService;

    @GetMapping("/notice")
    @Operation(summary = "공지사항 목록 조회")
    public BaseResponse<AdminReadAllNoticeResponse> readAllNotice(@PathVariable Long orgId) {
        return new BaseResponse<>(AdminReadAllNoticeResponse.of(noticeAdminService.readAllNotice(orgId)));
    }

    @GetMapping("/notice/{noticeId}")
    @Operation(summary = "공지사항 조회")
    public BaseResponse<AdminReadNoticeResponse> readNotice(@PathVariable Long orgId, @PathVariable Long noticeId) {
        return new BaseResponse<>(noticeAdminService.readNotice(orgId, noticeId));
    }

    @PostMapping("/notice")
    @Operation(summary = "공지사항 등록")
    public BaseResponse<SuccessResponse> createNotice(@PathVariable Long orgId,
                                                      @AuthenticationPrincipal(expression = "memberId") Long memberId,
                                                      @Valid @RequestBody CreateNoticeRequest createNoticeRequest) {
        return new BaseResponse<>(
                SuccessResponse.of(noticeAdminService.createNotice(orgId, memberId, createNoticeRequest)));
    }

    @DeleteMapping("/notice/{noticeId}")
    @Operation(summary = "공지사항 삭제")
    public BaseResponse<SuccessResponse> deleteNotice(@PathVariable Long orgId, @PathVariable Long noticeId) {
        return new BaseResponse<>(SuccessResponse.of(noticeAdminService.deleteNotice(orgId, noticeId)));
    }
}
