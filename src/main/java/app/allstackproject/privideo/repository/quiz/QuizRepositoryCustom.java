package app.allstackproject.privideo.repository.quiz;

import app.allstackproject.privideo.dto.quiz.MemberQuizDto;
import app.allstackproject.privideo.dto.video.QuizInfo;
import java.util.List;

public interface QuizRepositoryCustom {
    List<MemberQuizDto> findByMemberIdAndOrganizationId(Long memberId, Long orgId);

    List<QuizInfo> findByVideoId(Long videoId);
}
