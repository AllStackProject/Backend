package app.allstackproject.privideo.repository;

import app.allstackproject.privideo.common.enumStatus.BaseStatusType;
import app.allstackproject.privideo.entity.Comment;
import app.allstackproject.privideo.repository.comment.CommentRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {
    List<Comment> findByMemberIdAndVideoOrganizationId(Long memberId, Long orgId);

    List<Comment> findByVideoIdAndStatus(Long videoId, BaseStatusType status);
}
