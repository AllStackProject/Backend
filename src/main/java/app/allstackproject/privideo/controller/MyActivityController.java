package app.allstackproject.privideo.controller;

import app.allstackproject.privideo.common.enumStatus.AuthPrincipal;
import app.allstackproject.privideo.common.response.BaseResponse;
import app.allstackproject.privideo.common.response.SuccessResponse;
import app.allstackproject.privideo.dto.CommentResponse;
import app.allstackproject.privideo.dto.QuizResponse;
import app.allstackproject.privideo.dto.video.ScrapResponse;
import app.allstackproject.privideo.dto.video.HistoryResponse;
import app.allstackproject.privideo.service.MyCommentService;
import app.allstackproject.privideo.service.MyQuizService;
import app.allstackproject.privideo.service.video.MyScrapService;
import app.allstackproject.privideo.service.video.MyVideoService;
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
@RequestMapping("/myactivity")
@Slf4j
public class MyActivityController {

    private final MyVideoService myVideoService;
    private final MyQuizService myQuizService;
    private final MyScrapService myScrapService;
    private final MyCommentService myCommentService;

    //영상 시청 내역 조회
    @GetMapping("{orgId}/video")
    @PreAuthorize("hasAuthority('video:upload')")
    public BaseResponse<HistoryResponse> getVideoHistory(
            @AuthenticationPrincipal AuthPrincipal me,
            @PathVariable long orgId) {

        Long memberId = me.memberId();
        Long tokenOrgId = me.orgId();

        // 조직에 대한 토큰 일치 여부 검증
//        if (!tokenOrgId.equals(orgId)) {
//            throw new UnauthorizedException("해당 조직에 대한 권한이 없습니다.");
//        }

        HistoryResponse histories = myVideoService.getUserVideos(memberId, orgId);
        return new BaseResponse<>(histories);
    }

    //퀴즈 내역 조회
    @GetMapping("{orgId}/quiz")
    public BaseResponse<QuizResponse> getUserQuizses(
            @AuthenticationPrincipal AuthPrincipal me,
            @PathVariable long orgId) {

        Long memberId = me.memberId();

        QuizResponse quizzes = myQuizService.getUserQuizzes(memberId, orgId);
        return new BaseResponse<>(quizzes);
    }

    //스크랩 영상 리스트 조회
    @GetMapping("{orgId}/scrap")
    public BaseResponse<ScrapResponse> getUserScrabs(
            @AuthenticationPrincipal AuthPrincipal me, @PathVariable long orgId) {

        Long memberId = me.memberId();

        ScrapResponse scraps = myScrapService.getUserScraps(memberId, orgId);
        return new BaseResponse<>(scraps);
    }

    //사용자 댓글 조회
    @GetMapping("{orgId}/comment")
    public BaseResponse<CommentResponse> getUserComments(
            @AuthenticationPrincipal AuthPrincipal me,
            @PathVariable long orgId) {

        Long memberId = me.memberId();

        CommentResponse comments = myCommentService.getUserComments(memberId, orgId);
        return new BaseResponse<>(comments);
    }

    //사용자 댓글 삭제
    @DeleteMapping("{orgId}/{commentId}")
    public BaseResponse<SuccessResponse> deleteComment(
            @AuthenticationPrincipal AuthPrincipal me,
            @PathVariable Long orgId,
            @PathVariable Long commentId) {

        Long memberId = me.memberId();

        boolean result = myCommentService.deleteComment(memberId, orgId, commentId);
        return new BaseResponse<>(SuccessResponse.of(result));
    }
}

