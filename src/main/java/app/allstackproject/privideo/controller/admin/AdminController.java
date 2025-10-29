package app.allstackproject.privideo.controller.admin;

import app.allstackproject.privideo.common.response.BaseResponse;
import app.allstackproject.privideo.common.response.SuccessResponse;
import app.allstackproject.privideo.dto.organization.ChangeJoinStateRequest;
import app.allstackproject.privideo.service.admin.AdminService;
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
public class AdminController {

    private final AdminService adminService;

    @PreAuthorize("hasAuthority('org:admin')")
    @PatchMapping("/orgs/{orgId}/join")
    public BaseResponse<SuccessResponse> changeJoinState(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @Valid @RequestBody ChangeJoinStateRequest changeJoinStateRequest,
            @PathVariable Long orgId) {
        boolean isSuccess = adminService.changeJoinState(userId, orgId, changeJoinStateRequest);
        return new BaseResponse<>(SuccessResponse.of(isSuccess));
    }
}
