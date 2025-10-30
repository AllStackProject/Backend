package app.allstackproject.privideo.controller;

import app.allstackproject.privideo.common.enumStatus.AuthPrincipal;
import app.allstackproject.privideo.common.response.BaseResponse;
import app.allstackproject.privideo.common.response.SuccessResponse;
import app.allstackproject.privideo.dto.CommentResponse;
import app.allstackproject.privideo.dto.QuizResponse;
import app.allstackproject.privideo.dto.video.ScrapResponse;
import app.allstackproject.privideo.dto.video.HistoryResponse;
import app.allstackproject.privideo.service.CommentService;
import app.allstackproject.privideo.service.QuizService;
import app.allstackproject.privideo.service.video.ScrapService;
import app.allstackproject.privideo.service.video.HistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class MyActivityController {

    private final HistoryService historyService;
    private final QuizService quizService;
    private final ScrapService scrapService;
    private final CommentService commentService;

    //영상 시청 내역 조회
    @GetMapping("/video")
    public BaseResponse<HistoryResponse> getVideoHistory(
            @AuthenticationPrincipal AuthPrincipal me,
            @PathVariable long orgId) {

        Long memberId = me.memberId();

        HistoryResponse histories = historyService.getUserVideos(memberId, orgId);
        return new BaseResponse<>(histories);
    }

    //퀴즈 내역 조회
    @GetMapping("/quiz")
    public BaseResponse<QuizResponse> getUserQuizses(
            @AuthenticationPrincipal AuthPrincipal me,
            @PathVariable long orgId) {

        Long memberId = me.memberId();

        QuizResponse quizzes = quizService.getUserQuizzes(memberId, orgId);
        return new BaseResponse<>(quizzes);
    }

    //스크랩 영상 리스트 조회
    @GetMapping("/scrap")
    public BaseResponse<ScrapResponse> getUserScrabs(
            @AuthenticationPrincipal AuthPrincipal me, @PathVariable long orgId) {

        Long memberId = me.memberId();

        ScrapResponse scraps = scrapService.getUserScraps(memberId, orgId);
        return new BaseResponse<>(scraps);
    }

    //사용자 댓글 조회
    @GetMapping("/comment")
    public BaseResponse<CommentResponse> getUserComments(
            @AuthenticationPrincipal AuthPrincipal me,
            @PathVariable long orgId) {

        Long memberId = me.memberId();

        CommentResponse comments = commentService.getUserComments(memberId, orgId);
        return new BaseResponse<>(comments);
    }

    //사용자 댓글 삭제
    @DeleteMapping("/{commentId}")
    public BaseResponse<SuccessResponse> deleteComment(
            @AuthenticationPrincipal AuthPrincipal me,
            @PathVariable Long orgId,
            @PathVariable Long commentId) {

        Long memberId = me.memberId();

        boolean result = commentService.deleteComment(memberId, orgId, commentId);
        return new BaseResponse<>(SuccessResponse.of(result));
    }
}

