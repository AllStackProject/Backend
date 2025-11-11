package app.allstackproject.privideo.repository.quiz;

import app.allstackproject.privideo.common.enumStatus.BaseStatusType;
import app.allstackproject.privideo.entity.MemberQuizResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberQuizResultRepository extends JpaRepository<MemberQuizResult, Long> {
    boolean existsByQuizIdAndStatus(Long quizId, BaseStatusType status);
}
