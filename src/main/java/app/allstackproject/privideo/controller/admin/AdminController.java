package app.allstackproject.privideo.controller.admin;

import static app.allstackproject.privideo.common.config.SwaggerConfig.BOOTSTRAP_AUTH_KEY;

import app.allstackproject.privideo.common.response.BaseResponse;
import app.allstackproject.privideo.common.response.SuccessResponse;
import app.allstackproject.privideo.dto.organization.ChangeJoinStateRequest;
import app.allstackproject.privideo.dto.organization.OrgCodeResponse;
import app.allstackproject.privideo.dto.organization.UpdateMemberPermissionRequest;
import app.allstackproject.privideo.service.admin.AdminService;
import app.allstackproject.privideo.service.organization.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/{orgId}")
@Tag(name = "Admin", description = "관리자 관련 API")
@SecurityRequirement(name = BOOTSTRAP_AUTH_KEY)
public class AdminController {

    private final AdminService adminService;
    private final OrganizationService organizationService;

    @PreAuthorize("hasAuthority('org:admin')")
    @PatchMapping("/orgs/join")
    @Operation(summary = "조직 가입 요청 처리", description = "조직 가입 요청을 승인 또는 거절합니다.")
    public BaseResponse<SuccessResponse> changeJoinState(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @Valid @RequestBody ChangeJoinStateRequest changeJoinStateRequest,
            @PathVariable Long orgId) {
        boolean isSuccess = adminService.changeJoinState(userId, orgId, changeJoinStateRequest);
        return new BaseResponse<>(SuccessResponse.of(isSuccess));
    }

    @PreAuthorize("hasAuthority('org:admin')")
    @PutMapping("/orgs/perm")
    @Operation(summary = "멤버 권한 변경", description = "조직 멤버의 권한을 변경합니다.")
    public BaseResponse<SuccessResponse> updateMemberPermission(
            @AuthenticationPrincipal(expression = "userId") Long adminUserId,
            @PathVariable Long orgId,
            @Valid @RequestBody UpdateMemberPermissionRequest request) {

        boolean isSuccess = adminService.updateMemberPermission(
                adminUserId, orgId, request.getMemberId(), request.getPermissions());

        return new BaseResponse<>(SuccessResponse.of(isSuccess));
    }


    @PreAuthorize("hasAuthority('org:admin')")
    @PatchMapping("/orgs/code")
    @Operation(summary = "조직 코드 재발급", description = "조직 코드를 새로 발급합니다.")
    public BaseResponse<OrgCodeResponse> regenerateOrgToken(
            @AuthenticationPrincipal(expression = "userId") Long adminUserId,
            @PathVariable("orgId") Long orgId,
            HttpServletResponse response) {

        OrgCodeResponse newOrgCode = adminService.regenerateOrgCode(adminUserId, orgId);
        return new BaseResponse<>(newOrgCode);
    }
}
