package app.allstackproject.privideo.service;

import static app.allstackproject.privideo.common.enumStatus.BaseStatusType.ACTIVE;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.COMMENT_NOT_FOUND;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.COMMENT_UNAUTHORIZED_DELETE;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.INVALID_COMMENT_REQUEST;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.VIDEO_COMMENT_NOT_ALLOWED;

import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.dto.CommentResponse;
import app.allstackproject.privideo.dto.comment.CommentsResult;
import app.allstackproject.privideo.entity.Comment;
import app.allstackproject.privideo.repository.CommentRepository;
import app.allstackproject.privideo.repository.video.VideoRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final VideoRepository videoRepository;

    public CommentResponse getUserComments(Long memberId, Long orgId) {
        List<Comment> commentList = commentRepository.findByMemberIdAndVideoOrganizationId(memberId, orgId);
        return CommentResponse.of(commentList);
    }

    public boolean deleteComment(Long memberId, Long orgId, Long commentId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ApiException(COMMENT_NOT_FOUND));

        if (!comment.getMember().getId().equals(memberId)) {
            throw new ApiException(COMMENT_UNAUTHORIZED_DELETE);
        }

        commentRepository.deleteById(commentId);
        return true;
    }

    public CommentsResult readVideoComments(Long memberId, Long orgId, Long videoId) {
        if (!commentRepository.isValidMemberAndOrgAndVideo(memberId, orgId, videoId)) {
            throw new ApiException(INVALID_COMMENT_REQUEST);
        }
        if (!videoRepository.findById(videoId).get().isComment()) {
            throw new ApiException(VIDEO_COMMENT_NOT_ALLOWED);
        }

        List<Comment> comments = commentRepository.findByVideoIdAndStatus(videoId, ACTIVE);
        return CommentsResult.create(comments);
    }
}
