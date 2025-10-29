package app.allstackproject.privideo.service;

import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.COMMENT_NOT_FOUND;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.COMMENT_UNAUTHORIZED_DELETE;

import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.dto.CommentResponse;
import app.allstackproject.privideo.entity.Comment;
import app.allstackproject.privideo.repository.CommentRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MyCommentService {

    private final CommentRepository commentRepository;

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
}
