package app.allstackproject.privideo.controller;

import app.allstackproject.privideo.common.response.BaseResponse;
import app.allstackproject.privideo.dto.UserCommentResponse;
import app.allstackproject.privideo.dto.UserQuizResponse;
import app.allstackproject.privideo.dto.UserScrapResponse;
import app.allstackproject.privideo.dto.UserHistoryResponse;
import app.allstackproject.privideo.service.MyCommentService;
import app.allstackproject.privideo.service.MyQuizService;
import app.allstackproject.privideo.service.MyScrapService;
import app.allstackproject.privideo.service.MyVideoService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mypage")
@Slf4j
public class MyPageController {


    //@JWTAuth
    //private final JwtTokenProvider jwtTokenProvider;
    private final MyVideoService myVideoService;
    private final MyQuizService myQuizService;
    private final MyScrapService myScrapService;
    private final MyCommentService myCommentService;

    private final Long userId = 1L;


    //영상 시청 내역 조회
    @GetMapping("/video")
    public BaseResponse<UserHistoryResponse> getVideoHistory() {

        //        @RequestHeader("Authorization") String token) {
        //    String actualToken = token.replace("Bearer ", "");

        //jwtTokenProvider.getUserIdFromToken(actualToken);
        UserHistoryResponse histories = myVideoService.getUserVideos(userId);
        return new BaseResponse<>(histories);
    }

    //퀴즈 내역 조회
    @GetMapping("/quiz")
    public BaseResponse<UserQuizResponse> getUserQuizses() {
        UserQuizResponse quizzes = myQuizService.getUserQuizzes(userId);
        return new BaseResponse<>(quizzes);
    }

    //스크랩 영상 리스트 조회
    @GetMapping("/scrap")
    public BaseResponse<UserScrapResponse> getUserScrabs() {
        UserScrapResponse scrabs = myScrapService.getUserScraps(userId);
        return new BaseResponse<>(scrabs);
    }

    //사용자 댓글 조회
    @GetMapping("/comment")
    public BaseResponse<UserCommentResponse> getUserComments() {
        UserCommentResponse comments = myCommentService.getUserComments(userId);
        return new BaseResponse<>(comments);
    }

    //사용자 댓글 삭제
    @DeleteMapping("/{commentId}")
    public BaseResponse<Map<String, Boolean>> deleteComment(@PathVariable Long commentId) {
        boolean result = myCommentService.deleteComment(commentId);
        Map<String, Boolean> response = Map.of("is_success", result);
        return new BaseResponse<>(response);
    }

    //사용자 정보 조회
//    @GetMapping("/info/{userId}")
//    public BaseResponse<UserInfoResponse> getMyInfo(@RequestParam Long userId) {
//        UserInfoResponse infos = myInfoService.getUserInfos(userId);
//        return new BaseResponse<>(infos);
//    }
}

