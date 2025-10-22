package app.allstackproject.privideo.controller.user;

import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.INVALID_USER_SIGNUP;
import static app.allstackproject.privideo.common.util.BindingResultUtil.getErrorMessage;

import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.common.response.BaseResponse;
import app.allstackproject.privideo.common.response.SuccessResponse;
import app.allstackproject.privideo.dto.user.PostSignupRequest;
import app.allstackproject.privideo.service.user.UserService;
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
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public BaseResponse<SuccessResponse> signup(@Valid @RequestBody PostSignupRequest postSignupRequest,
                                                BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ApiException(INVALID_USER_SIGNUP, getErrorMessage(bindingResult));
        }

        boolean isSuccess = userService.signup(postSignupRequest);
        return new BaseResponse<>(SuccessResponse.of(isSuccess));
    }

}
