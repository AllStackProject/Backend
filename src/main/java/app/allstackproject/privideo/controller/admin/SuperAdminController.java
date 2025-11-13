package app.allstackproject.privideo.controller.admin;

import static app.allstackproject.privideo.common.config.SwaggerConfig.ORG_AUTH_KEY;

import app.allstackproject.privideo.common.response.BaseResponse;
import app.allstackproject.privideo.common.response.SuccessResponse;
import app.allstackproject.privideo.dto.admin.ReadAllMemberResponse;
import app.allstackproject.privideo.dto.organization.ChangeJoinStateRequest;
import app.allstackproject.privideo.dto.organization.UpdateMemberPermissionRequest;
import app.allstackproject.privideo.service.admin.SuperAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/org/{orgId}")
@PreAuthorize("hasAuthority('org:admin')")
@Tag(name = "Admin-Super", description = "슈퍼 관리자 관련 API")
@SecurityRequirement(name = ORG_AUTH_KEY)
public class SuperAdminController {

    private final SuperAdminService superAdminService;

    @GetMapping("/members")
    @Operation(summary = "조직 내 전체 멤버 조회")
    public BaseResponse<ReadAllMemberResponse> readAllMember(@PathVariable Long orgId) {
        return new BaseResponse<>(ReadAllMemberResponse.of(superAdminService.readAllMember(orgId)));
    }

    @PutMapping("/member/{memberId}/perm")
    @Operation(summary = "조직 멤버의 권한 수정")
    public BaseResponse<SuccessResponse> updateMemberPermission(@PathVariable Long orgId, @PathVariable Long memberId,
                                                                @Valid @RequestBody UpdateMemberPermissionRequest request) {

        boolean isSuccess = superAdminService.updateMemberPermission(memberId, orgId, request);

        return new BaseResponse<>(SuccessResponse.of(isSuccess));
    }

    @PutMapping("/member/{memberId}/group")
    @Operation(summary = "조직 멤버의 멤버 그룹 수정")
    public BaseResponse<SuccessResponse> modifyMemberGroup(@PathVariable Long orgId, @PathVariable Long memberId,
                                                           @Valid @RequestBody UpdateMemberPermissionRequest request) {

        boolean isSuccess = superAdminService.updateMemberPermission(memberId, orgId, request);

        return new BaseResponse<>(SuccessResponse.of(isSuccess));
    }

    @PatchMapping("/orgs/join")
    @Operation(summary = "조직 가입 요청 처리", description = "조직 가입 요청을 승인 또는 거절합니다.")
    public BaseResponse<SuccessResponse> changeJoinState(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @Valid @RequestBody ChangeJoinStateRequest changeJoinStateRequest,
            @PathVariable Long orgId) {
        boolean isSuccess = superAdminService.changeJoinState(userId, orgId, changeJoinStateRequest);
        return new BaseResponse<>(SuccessResponse.of(isSuccess));
    }
}
