package app.allstackproject.privideo.service;

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
        //검증 로직 필요

        commentRepository.deleteById(commentId);
        return true;
    }
}
