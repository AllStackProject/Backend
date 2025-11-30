package app.allstackproject.privideo.domain.comment.controller;

import static app.allstackproject.privideo.global.config.SwaggerConfig.ORG_AUTH_KEY;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.INVALID_COMMENT_CREATE;
import static app.allstackproject.privideo.global.util.BindingResultUtil.getErrorMessage;

import app.allstackproject.privideo.global.exception.ApiException;
import app.allstackproject.privideo.global.response.BaseResponse;
import app.allstackproject.privideo.global.response.SuccessResponse;
import app.allstackproject.privideo.domain.comment.dto.response.CommentsResult;
import app.allstackproject.privideo.domain.comment.dto.request.CreateCommentRequest;
import app.allstackproject.privideo.domain.comment.dto.response.ReadCommentsResponse;
import app.allstackproject.privideo.domain.comment.service.CommentService;
import app.allstackproject.privideo.shared.enums.AuthPrincipal;
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
@RequestMapping("/{orgId}/video/{videoId}")
@PreAuthorize("hasAuthority('org:granted')")
@Tag(name = "Comment", description = "댓글 관련 API")
@SecurityRequirement(name = ORG_AUTH_KEY)
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/comments")
    @Operation(summary = "댓글 목록 조회")
    public BaseResponse<ReadCommentsResponse> readComments(
            @AuthenticationPrincipal AuthPrincipal me,
            @PathVariable Long orgId,
            @PathVariable Long videoId) {
        CommentsResult commentsResult = commentService.readVideoComments(me.memberId(), orgId, videoId);
        return new BaseResponse<>(ReadCommentsResponse.of(commentsResult));
    }

    @PostMapping("/comment")
    @Operation(summary = "댓글 작성")
    public BaseResponse<SuccessResponse> createComment(
            @AuthenticationPrincipal AuthPrincipal me,
            @PathVariable Long orgId,
            @PathVariable Long videoId,
            @Valid @RequestBody CreateCommentRequest createCommentRequest,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ApiException(INVALID_COMMENT_CREATE, getErrorMessage(bindingResult));
        }

        return new BaseResponse<>(
                SuccessResponse.of(commentService.createComment(me.memberId(), orgId, videoId, createCommentRequest)));
    }
}
