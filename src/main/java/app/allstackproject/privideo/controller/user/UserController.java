package app.allstackproject.privideo.controller.user;

import static app.allstackproject.privideo.common.filter.JwtAuthFilter.ACCESS_TOKEN_HEADER;
import static app.allstackproject.privideo.common.filter.JwtAuthFilter.TOKEN_PREFIX;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.INVALID_USER_LOGIN;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.INVALID_USER_SIGNUP;
import static app.allstackproject.privideo.common.util.BindingResultUtil.getErrorMessage;

import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.common.response.BaseResponse;
import app.allstackproject.privideo.common.response.SuccessResponse;
import app.allstackproject.privideo.dto.user.PatchPasswordRequest;
import app.allstackproject.privideo.dto.user.PostLoginRequest;
import app.allstackproject.privideo.dto.user.PostSignupRequest;
import app.allstackproject.privideo.service.user.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public BaseResponse<SuccessResponse> postSignup(@Valid @RequestBody PostSignupRequest postSignupRequest,
                                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ApiException(INVALID_USER_SIGNUP, getErrorMessage(bindingResult));
        }

        boolean isSuccess = userService.signup(postSignupRequest);
        return new BaseResponse<>(SuccessResponse.of(isSuccess));
    }

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
    @PatchMapping("/password")
    public BaseResponse<SuccessResponse> patchPassword(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @Valid @RequestBody PatchPasswordRequest patchPasswordRequest) {
        boolean isSuccess = userService.patchPassword(userId, patchPasswordRequest);
        return new BaseResponse<>(SuccessResponse.of(isSuccess));
    }
}
