package app.allstackproject.privideo.repository.quiz.custom;

import app.allstackproject.privideo.dto.video.QuizInfo;
import java.util.List;

public interface QuizRepositoryCustom {
    List<QuizInfo> findAllByVideoId(Long videoId);
}
