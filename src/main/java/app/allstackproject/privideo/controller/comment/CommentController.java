package app.allstackproject.privideo.controller.comment;

import static app.allstackproject.privideo.common.config.SwaggerConfig.ORG_AUTH_KEY;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.INVALID_COMMENT_CREATE;
import static app.allstackproject.privideo.common.util.BindingResultUtil.getErrorMessage;

import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.common.response.BaseResponse;
import app.allstackproject.privideo.common.response.SuccessResponse;
import app.allstackproject.privideo.dto.comment.CommentsResult;
import app.allstackproject.privideo.dto.comment.CreateCommentRequest;
import app.allstackproject.privideo.dto.comment.ReadCommentsResponse;
import app.allstackproject.privideo.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/{orgId}/{videoId}/comment")
@PreAuthorize("hasAuthority('org:granted')")
@Tag(name = "Comment", description = "댓글 관련 API")
@SecurityRequirement(name = ORG_AUTH_KEY)
public class CommentController {

    private final CommentService commentService;

    @GetMapping("")
    @Operation(summary = "댓글 전체 조회")
    public BaseResponse<ReadCommentsResponse> readComments(
            @AuthenticationPrincipal(expression = "memberId") Long memberId, @PathVariable("orgId") Long orgId,
            @PathVariable("videoId") Long videoId) {
        CommentsResult commentsResult = commentService.readVideoComments(memberId, orgId, videoId);
        return new BaseResponse<>(ReadCommentsResponse.of(commentsResult));
    }

    @PostMapping("")
    @Operation(summary = "댓글 작성")
    public BaseResponse<SuccessResponse> createComment(
            @AuthenticationPrincipal(expression = "memberId") Long memberId, @PathVariable("orgId") Long orgId,
            @PathVariable("videoId") Long videoId, @Valid @RequestBody CreateCommentRequest createCommentRequest,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ApiException(INVALID_COMMENT_CREATE, getErrorMessage(bindingResult));
        }

        boolean result = commentService.createComment(memberId, orgId, videoId, createCommentRequest);
        return new BaseResponse<>(SuccessResponse.of(result));
    }
}
