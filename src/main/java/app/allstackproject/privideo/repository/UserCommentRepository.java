package app.allstackproject.privideo.repository;

import app.allstackproject.privideo.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByMemberId(Long memberId);
}
