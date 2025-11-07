package app.allstackproject.privideo.controller.quiz;

import static app.allstackproject.privideo.common.config.SwaggerConfig.ORG_AUTH_KEY;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.INVALID_SOLVE_REQUEST;
import static app.allstackproject.privideo.common.util.BindingResultUtil.getErrorMessage;

import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.common.response.BaseResponse;
import app.allstackproject.privideo.common.response.SuccessResponse;
import app.allstackproject.privideo.dto.comment.CreateCommentRequest;
import app.allstackproject.privideo.dto.quiz.SolveQuizRequest;
import app.allstackproject.privideo.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/{orgId}/video/{videoId}/quiz")
@PreAuthorize("hasAuthority('org:granted')")
@Tag(name = "Quiz", description = "퀴즈 관련 API")
@SecurityRequirement(name = ORG_AUTH_KEY)
public class QuizController {

    private final QuizService quizService;

    @PostMapping("")
    @Operation(summary = "퀴즈 제출")
    public BaseResponse<SuccessResponse> solveQuiz(@AuthenticationPrincipal(expression = "memberId") Long memberId,
                                                   @PathVariable("orgId") Long orgId,
                                                   @PathVariable("videoId") Long videoId,
                                                   @Valid @RequestBody SolveQuizRequest solveQuizRequest,
                                                   BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ApiException(INVALID_SOLVE_REQUEST, getErrorMessage(bindingResult));
        }

        boolean result = quizService.createQuizResult(memberId, orgId, videoId, solveQuizRequest.getAnswers());
        return new BaseResponse<>(SuccessResponse.of(result));
    }
}
