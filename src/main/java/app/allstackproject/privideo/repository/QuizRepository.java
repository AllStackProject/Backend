package app.allstackproject.privideo.repository;

import app.allstackproject.privideo.entity.MemberQuizResult;
import app.allstackproject.privideo.repository.quiz.QuizRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<MemberQuizResult, Long>, QuizRepositoryCustom {
    List<MemberQuizResult> findByMemberIdAndVideo_OrganizationId(Long memberId, Long organizationId);
}