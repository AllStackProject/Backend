package app.allstackproject.privideo.controller.admin;

import static app.allstackproject.privideo.common.config.SwaggerConfig.ORG_AUTH_KEY;

import app.allstackproject.privideo.common.response.BaseResponse;
import app.allstackproject.privideo.dto.admin.ReadAllNoticeResponse;
import app.allstackproject.privideo.dto.admin.ReadNoticeResponse;
import app.allstackproject.privideo.service.admin.NoticeAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    public BaseResponse<ReadAllNoticeResponse> readAllNotice(@PathVariable Long orgId) {
        return new BaseResponse<>(ReadAllNoticeResponse.of(noticeAdminService.readAllNotice(orgId)));
    }

    @GetMapping("/notice/{noticeId}")
    @Operation(summary = "공지사항 조회")
    public BaseResponse<ReadNoticeResponse> readNotice(@PathVariable Long orgId, @PathVariable Long noticeId) {
        return new BaseResponse<>(noticeAdminService.readNotice(orgId, noticeId));
    }
}
