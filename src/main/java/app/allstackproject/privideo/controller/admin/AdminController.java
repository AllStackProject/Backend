package app.allstackproject.privideo.controller.admin;

import static app.allstackproject.privideo.common.config.SwaggerConfig.BOOTSTRAP_AUTH_KEY;

import app.allstackproject.privideo.common.response.BaseResponse;
import app.allstackproject.privideo.common.response.SuccessResponse;
import app.allstackproject.privideo.dto.organization.ChangeJoinStateRequest;
import app.allstackproject.privideo.service.admin.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
@Tag(name = "Admin", description = "관리자 관련 API")
@SecurityRequirement(name = BOOTSTRAP_AUTH_KEY)
public class AdminController {

    private final AdminService adminService;

    @PreAuthorize("hasAuthority('org:admin')")
    @PatchMapping("/orgs/{orgId}/join")
    @Operation(summary = "조직 가입 요청 처리", description = "조직 가입 요청을 승인 또는 거절합니다.")
    public BaseResponse<SuccessResponse> changeJoinState(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @Valid @RequestBody ChangeJoinStateRequest changeJoinStateRequest,
            @PathVariable Long orgId) {
        boolean isSuccess = adminService.changeJoinState(userId, orgId, changeJoinStateRequest);
        return new BaseResponse<>(SuccessResponse.of(isSuccess));
    }
}
