package app.allstackproject.privideo.controller;

import static app.allstackproject.privideo.common.config.SwaggerConfig.BOOTSTRAP_AUTH_KEY;

import app.allstackproject.privideo.common.enumStatus.AuthPrincipal;
import app.allstackproject.privideo.common.response.BaseResponse;
import app.allstackproject.privideo.common.response.SuccessResponse;
import app.allstackproject.privideo.dto.user.UpdateUserInfoRequest;
import app.allstackproject.privideo.dto.user.UserInfoResponse;
import app.allstackproject.privideo.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mypage")
@Slf4j
@Tag(name = "MyPage", description = "마이페이지 관련 API")
@SecurityRequirement(name = BOOTSTRAP_AUTH_KEY)
public class MyPageController {

    private final UserService userService;

    @GetMapping("/info")
    @Operation(summary = "사용자 정보 조회")
    public BaseResponse<UserInfoResponse> getMyInfo(
            @AuthenticationPrincipal AuthPrincipal me) {

        Long useerId = me.userId();
        UserInfoResponse user = userService.getUserInfo(useerId);
        return new BaseResponse<>(user);
    }

    @PatchMapping("/info")
    @Operation(summary = "사용자 정보 수정")
    public BaseResponse<SuccessResponse> updateMyInfo(
            @AuthenticationPrincipal AuthPrincipal me,
            @RequestBody @Valid UpdateUserInfoRequest request) {

        Long userId = me.userId();
        boolean result = userService.updateUserInfo(userId, request);
        return new BaseResponse<>(SuccessResponse.of(result));
    }
}

