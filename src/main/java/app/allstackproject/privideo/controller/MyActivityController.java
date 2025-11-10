package app.allstackproject.privideo.controller;

import static app.allstackproject.privideo.common.config.SwaggerConfig.ORG_AUTH_KEY;

import app.allstackproject.privideo.common.enumStatus.AuthPrincipal;
import app.allstackproject.privideo.common.response.BaseResponse;
import app.allstackproject.privideo.common.response.SuccessResponse;
import app.allstackproject.privideo.dto.comment.CommentResponse;
import app.allstackproject.privideo.dto.quiz.QuizResponse;
import app.allstackproject.privideo.dto.scrap.ScrapResponse;
import app.allstackproject.privideo.dto.history.HistoryResponse;
import app.allstackproject.privideo.service.CommentService;
import app.allstackproject.privideo.service.QuizService;
import app.allstackproject.privideo.service.video.ScrapService;
import app.allstackproject.privideo.service.video.HistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("{orgId}/myactivity")
@Slf4j
@PreAuthorize("hasAuthority('org:granted')") // TODO: org에 가입 완료된 상태로 변경
@Tag(name = "MyActivity", description = "내 활동 관련 API")
@SecurityRequirement(name = ORG_AUTH_KEY)
public class MyActivityController {

    private final HistoryService historyService;
    private final QuizService quizService;
    private final ScrapService scrapService;
    private final CommentService commentService;

    @GetMapping("/video")
    @Operation(summary = "영상 시청 기록 조회")
    public BaseResponse<HistoryResponse> getVideoHistory(
            @AuthenticationPrincipal AuthPrincipal me,
            @PathVariable long orgId) {

        Long memberId = me.memberId();

        HistoryResponse histories = historyService.getUserVideos(memberId, orgId);
        return new BaseResponse<>(histories);
    }

    @GetMapping("/quiz")
    @Operation(summary = "AI 퀴즈 기록 조회")
    public BaseResponse<QuizResponse> getUserQuizses(
            @AuthenticationPrincipal AuthPrincipal me,
            @PathVariable long orgId) {

        Long memberId = me.memberId();

        QuizResponse quizzes = quizService.getUserQuizzes(memberId, orgId);
        return new BaseResponse<>(quizzes);
    }

    @GetMapping("/scrap")
    @Operation(summary = "스크랩한 영상 조회")
    public BaseResponse<ScrapResponse> getUserScrabs(
            @AuthenticationPrincipal AuthPrincipal me, @PathVariable long orgId) {

        Long memberId = me.memberId();

        ScrapResponse scraps = scrapService.getUserScraps(memberId, orgId);
        return new BaseResponse<>(scraps);
    }

    @GetMapping("/comment")
    @Operation(summary = "작성한 댓글 조회")
    public BaseResponse<CommentResponse> getUserComments(
            @AuthenticationPrincipal AuthPrincipal me,
            @PathVariable long orgId) {

        Long memberId = me.memberId();

        CommentResponse comments = commentService.getUserComments(memberId, orgId);
        return new BaseResponse<>(comments);
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "작성한 댓글 삭제")
    public BaseResponse<SuccessResponse> deleteComment(
            @AuthenticationPrincipal AuthPrincipal me,
            @PathVariable Long orgId,
            @PathVariable Long commentId) {

        Long memberId = me.memberId();

        boolean result = commentService.deleteComment(memberId, orgId, commentId);
        return new BaseResponse<>(SuccessResponse.of(result));
    }
}

