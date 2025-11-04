package app.allstackproject.privideo.repository;

import app.allstackproject.privideo.dto.video.QuizInfo;
import app.allstackproject.privideo.entity.MemberQuizResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<MemberQuizResult, Long> {
    List<MemberQuizResult> findByMemberIdAndVideo_OrganizationId(Long memberId, Long organizationId);

    List<QuizInfo> findByVideoId(Long videoId);
}