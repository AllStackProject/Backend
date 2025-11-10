package app.allstackproject.privideo.service;

import static app.allstackproject.privideo.common.enumStatus.BaseStatusType.ACTIVE;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.COMMENT_NOT_FOUND;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.COMMENT_UNAUTHORIZED_DELETE;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.INVALID_COMMENT_REQUEST;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.PARENT_COMMENT_NOT_FOUND;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.VIDEO_COMMENT_NOT_ALLOWED;

import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.dto.comment.CommentResponse;
import app.allstackproject.privideo.dto.comment.CommentsResult;
import app.allstackproject.privideo.dto.comment.CreateCommentRequest;
import app.allstackproject.privideo.entity.Comment;
import app.allstackproject.privideo.repository.comment.CommentRepository;
import app.allstackproject.privideo.repository.member.MemberRepository;
import app.allstackproject.privideo.repository.video.VideoRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final VideoRepository videoRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public CommentsResult readVideoComments(Long memberId, Long orgId, Long videoId) {
        if (!videoRepository.isValidMemberAndOrgAndVideo(memberId, orgId, videoId)) {
            throw new ApiException(INVALID_COMMENT_REQUEST);
        }

        if (!videoRepository.findById(videoId).get().isComment()) {
            throw new ApiException(VIDEO_COMMENT_NOT_ALLOWED);
        }

        List<Comment> comments = commentRepository.findByVideoIdAndStatus(videoId, ACTIVE);
        return CommentsResult.create(comments);
    }

    public boolean createComment(Long memberId, Long orgId, Long videoId,
                                 @Valid CreateCommentRequest createCommentRequest) {
        if (!videoRepository.isValidMemberAndOrgAndVideo(memberId, orgId, videoId)) {
            throw new ApiException(INVALID_COMMENT_REQUEST);
        }
        if (!videoRepository.findById(videoId).get().isComment()) {
            throw new ApiException(VIDEO_COMMENT_NOT_ALLOWED);
        }

        boolean isChild = createCommentRequest.getParentCommentId() != null;
        Long parentCommentId = createCommentRequest.getParentCommentId();
        if (isChild) {
            Optional<Comment> parentComment = commentRepository.findById(parentCommentId);
            if (parentComment.isEmpty() || !parentComment.get().getVideo().getId().equals(videoId)) {
                throw new ApiException(PARENT_COMMENT_NOT_FOUND);
            }
        }

        Comment comment = Comment.create(videoRepository.getReferenceById(videoId),
                memberRepository.getReferenceById(memberId), createCommentRequest.getText(), isChild, parentCommentId);
        commentRepository.save(comment);

        return true;
    }
}
