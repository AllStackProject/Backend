package app.allstackproject.privideo.controller.admin;

import static app.allstackproject.privideo.common.config.SwaggerConfig.ORG_AUTH_KEY;

import app.allstackproject.privideo.common.response.BaseResponse;
import app.allstackproject.privideo.dto.organization.OrgCodeResponse;
import app.allstackproject.privideo.service.admin.OrgAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/org/{orgId}")
@PreAuthorize("hasAuthority('org:granted') and hasAuthority('perm:org_setting')")
@Tag(name = "Admin-Org", description = "관리자 조직 설정 API")
@SecurityRequirement(name = ORG_AUTH_KEY)
public class OrgAdminController {

    private final OrgAdminService orgAdminService;

    @PatchMapping("/orgs/code")
    @Operation(summary = "조직 코드 재발급", description = "조직 코드를 새로 발급합니다.")
    public BaseResponse<OrgCodeResponse> regenerateOrgToken(
            @AuthenticationPrincipal(expression = "memberId") Long memberId, @PathVariable("orgId") Long orgId) {
        OrgCodeResponse newOrgCode = orgAdminService.regenerateOrgCode(memberId, orgId);
        return new BaseResponse<>(newOrgCode);
    }
}
