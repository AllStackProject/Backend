package app.allstackproject.privideo.repository.comment;

import app.allstackproject.privideo.entity.Comment;
import app.allstackproject.privideo.repository.comment.custom.CommentRepositoryCustom;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {
    @EntityGraph(attributePaths = {"video"})
    List<Comment> findByMemberIdAndVideoOrganizationId(Long memberId, Long orgId);

    void deleteAllByParentCommentId(Long parentCommentId);
}
