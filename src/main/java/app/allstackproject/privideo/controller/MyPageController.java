package app.allstackproject.privideo.controller;

import app.allstackproject.privideo.common.enumStatus.AuthPrincipal;
import app.allstackproject.privideo.common.response.BaseResponse;
import app.allstackproject.privideo.dto.CommentResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    //사용자 정보 조회
    @GetMapping("/info")
    public BaseResponse<UserInfoResponse> getMyInfo(
            @AuthenticationPrincipal AuthPrincipal me) {

        Long memberId = me.memberId();
        UserInfoResponse info = myInfoService.getUserInfo(memberId);
        return new BaseResponse<>(info);
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
}

