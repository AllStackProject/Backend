package app.allstackproject.privideo.repository.quiz.custom;

import static app.allstackproject.privideo.common.enumStatus.BaseStatusType.ACTIVE;
import static app.allstackproject.privideo.entity.QQuiz.quiz;
import static app.allstackproject.privideo.entity.QVideo.video;

import app.allstackproject.privideo.dto.video.QuizInfo;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class QuizRepositoryImpl implements QuizRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<QuizInfo> findByVideoId(Long videoId) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        QuizInfo.class,
                        quiz.id,
                        quiz.question,
                        quiz.answer,
                        quiz.description
                ))
                .from(quiz)
                .join(quiz.video, video)
                .where(video.id.eq(videoId))
                .fetch();
    }

}
