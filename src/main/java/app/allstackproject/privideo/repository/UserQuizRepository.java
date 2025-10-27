package app.allstackproject.privideo.repository;

import app.allstackproject.privideo.entity.MemberQuizResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserQuizRepository extends JpaRepository<MemberQuizResult, Long> {
    List<MemberQuizResult> findByMemberId(Long memberId);
}