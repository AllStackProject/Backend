package app.allstackproject.privideo.controller.comment;

import app.allstackproject.privideo.common.response.BaseResponse;
import app.allstackproject.privideo.dto.comment.CommentsResult;
import app.allstackproject.privideo.dto.comment.ReadCommentsResponse;
import app.allstackproject.privideo.service.CommentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/{orgId}/{videoId}/comment")
@Tag(name = "Comment", description = "댓글 관련 API")
public class CommentController {

    private final CommentService commentService;

    @GetMapping("")
    public BaseResponse<ReadCommentsResponse> readComments(
            @AuthenticationPrincipal(expression = "memberId") Long memberId, @PathVariable("orgId") Long orgId,
            @PathVariable("videoId") Long videoId) {
        CommentsResult commentsResult = commentService.readVideoComments(memberId, orgId, videoId);
        return new BaseResponse<>(ReadCommentsResponse.of(commentsResult));
    }
}
