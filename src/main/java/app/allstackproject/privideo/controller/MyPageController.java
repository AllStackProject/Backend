package app.allstackproject.privideo.controller;

import app.allstackproject.privideo.common.response.BaseResponse;
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

//    private final MyVideoService myVideoService;
//    private final MyQuizService myQuizService;
//    private final MyScrapService myScrapService;
//    private final MyCommentService myCommentService;

    //사용자 정보 조회
//    @GetMapping("/info/{userId}")
//    public BaseResponse<UserInfoResponse> getMyInfo(@RequestParam Long userId) {
//        UserInfoResponse infos = myInfoService.getUserInfos(userId);
//        return new BaseResponse<>(infos);
//    }
}

