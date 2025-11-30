package app.allstackproject.privideo.domain.quiz.repository.custom;

import static app.allstackproject.privideo.domain.quiz.entity.QQuiz.quiz;
import static app.allstackproject.privideo.domain.video.entity.QVideo.video;
import static app.allstackproject.privideo.domain.video.enums.UploadStatusType.COMPLETE;

import app.allstackproject.privideo.domain.quiz.dto.QuizInfo;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class QuizRepositoryImpl implements QuizRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<QuizInfo> findAllByVideoId(Long videoId) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        QuizInfo.class,
                        quiz.question,
                        quiz.answer,
                        quiz.description
                ))
                .from(quiz)
                .join(quiz.video, video)
                .where(
                        video.id.eq(videoId),
                        video.uploadStatus.eq(COMPLETE)
                )
                .fetch();
    }

}
