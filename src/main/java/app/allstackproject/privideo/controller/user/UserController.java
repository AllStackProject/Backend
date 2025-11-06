package app.allstackproject.privideo.controller.user;

import static app.allstackproject.privideo.common.filter.JwtAuthFilter.ACCESS_TOKEN_HEADER;
import static app.allstackproject.privideo.common.filter.JwtAuthFilter.TOKEN_PREFIX;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.INVALID_USER_LOGIN;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.INVALID_USER_SIGNUP;
import static app.allstackproject.privideo.common.util.BindingResultUtil.getErrorMessage;

import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.common.response.BaseResponse;
import app.allstackproject.privideo.common.response.SuccessResponse;
import app.allstackproject.privideo.dto.user.PostLoginRequest;
import app.allstackproject.privideo.dto.user.PostSignupRequest;
import app.allstackproject.privideo.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Tag(name = "User", description = "유저 관련 API")
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
}
