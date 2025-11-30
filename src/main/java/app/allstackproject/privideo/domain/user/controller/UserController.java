package app.allstackproject.privideo.domain.user.controller;

import static app.allstackproject.privideo.global.config.SwaggerConfig.BOOTSTRAP_AUTH_KEY;
import static app.allstackproject.privideo.global.security.JwtAuthFilter.ACCESS_TOKEN_HEADER;
import static app.allstackproject.privideo.global.security.JwtAuthFilter.TOKEN_PREFIX;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.INVALID_USER_LOGIN;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.INVALID_USER_SIGNUP;
import static app.allstackproject.privideo.global.util.BindingResultUtil.getErrorMessage;

import app.allstackproject.privideo.shared.enums.AuthPrincipal;
import app.allstackproject.privideo.global.exception.ApiException;
import app.allstackproject.privideo.global.response.BaseResponse;
import app.allstackproject.privideo.global.response.SuccessResponse;
import app.allstackproject.privideo.domain.user.dto.request.PostLoginRequest;
import app.allstackproject.privideo.domain.user.dto.request.PostSignupRequest;
import app.allstackproject.privideo.domain.user.dto.request.UpdateUserInfoRequest;
import app.allstackproject.privideo.domain.user.dto.response.UserInfoResponse;
import app.allstackproject.privideo.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Tag(name = "User", description = "유저 관련 API")
@SecurityRequirement(name = BOOTSTRAP_AUTH_KEY)
public class UserController {

    private final UserService userService;

    @Operation(summary = "유저 회원가입")
    @PostMapping("/signup")
    public BaseResponse<SuccessResponse> postSignup(@Valid @RequestBody PostSignupRequest postSignupRequest,
                                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ApiException(INVALID_USER_SIGNUP, getErrorMessage(bindingResult));
        }

        boolean isSuccess = userService.signup(postSignupRequest);
        return new BaseResponse<>(SuccessResponse.of(isSuccess));
    }

    @Operation(summary = "유저 로그인")
    @PostMapping("/login")
    public BaseResponse<SuccessResponse> postLogin(@Valid @RequestBody PostLoginRequest postLoginRequest,
                                                   BindingResult bindingResult, HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            throw new ApiException(INVALID_USER_LOGIN, getErrorMessage(bindingResult));
        }

        String accessToken = userService.login(postLoginRequest);
        if (accessToken == null || accessToken.isBlank()) {
            return new BaseResponse<>(SuccessResponse.of(false));
        }

        response.setHeader(ACCESS_TOKEN_HEADER, TOKEN_PREFIX + accessToken);
        return new BaseResponse<>(SuccessResponse.of(true));
    }

    @PreAuthorize("hasAuthority('bootstrap:granted')")
    @GetMapping("/info")
    @Operation(summary = "유저 정보 조회")
    public BaseResponse<UserInfoResponse> getMyInfo(
            @AuthenticationPrincipal AuthPrincipal me) {

        Long useerId = me.userId();
        UserInfoResponse user = userService.getUserInfo(useerId);
        return new BaseResponse<>(user);
    }

    @PreAuthorize("hasAuthority('bootstrap:granted')")
    @PatchMapping("/info")
    @Operation(summary = "유저 정보 수정")
    public BaseResponse<SuccessResponse> updateMyInfo(
            @AuthenticationPrincipal AuthPrincipal me,
            @RequestBody @Valid UpdateUserInfoRequest request) {

        Long userId = me.userId();
        boolean result = userService.updateUserInfo(userId, request);
        return new BaseResponse<>(SuccessResponse.of(result));
    }

    @PreAuthorize("hasAuthority('bootstrap:granted')")
    @DeleteMapping("")
    @Operation(summary = "유저 탈퇴")
    public BaseResponse<SuccessResponse> deleteUser(@AuthenticationPrincipal(expression = "userId") Long userId) {
        return new BaseResponse<>(SuccessResponse.of(userService.deleteUser(userId)));
    }
}
