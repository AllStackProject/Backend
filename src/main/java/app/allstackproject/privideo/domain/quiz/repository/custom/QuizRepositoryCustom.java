package app.allstackproject.privideo.domain.quiz.repository.custom;

import app.allstackproject.privideo.dto.video.QuizInfo;
import java.util.List;

public interface QuizRepositoryCustom {
    List<QuizInfo> findAllByVideoId(Long videoId);
}
