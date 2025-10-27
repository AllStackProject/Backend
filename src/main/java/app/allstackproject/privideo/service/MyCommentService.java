package app.allstackproject.privideo.service;

import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus;
import app.allstackproject.privideo.dto.UserCommentResponse;
import app.allstackproject.privideo.entity.Comment;
import app.allstackproject.privideo.repository.UserCommentRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MyCommentService {

    private final UserCommentRepository userCommentRepository;

    public UserCommentResponse getUserComments(Long memberId) {
        List<Comment> commentList = userCommentRepository.findByMemberId(memberId);
        return UserCommentResponse.of(commentList);
    }

    public boolean deleteComment(Long commentId) {
        if (!userCommentRepository.existsById(commentId)) {
            throw new ApiException(BaseExceptionResponseStatus.BAD_REQUEST);
        }
        userCommentRepository.deleteById(commentId);
        return true;
    }
}
