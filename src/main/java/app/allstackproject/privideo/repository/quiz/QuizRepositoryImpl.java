package app.allstackproject.privideo.repository.quiz;

import static app.allstackproject.privideo.common.enumStatus.BaseStatusType.ACTIVE;
import static app.allstackproject.privideo.entity.QMemberQuizResult.memberQuizResult;
import static app.allstackproject.privideo.entity.QQuiz.quiz;
import static app.allstackproject.privideo.entity.QVideo.video;

import app.allstackproject.privideo.dto.quiz.MemberQuizDto;
import app.allstackproject.privideo.dto.video.QuizInfo;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class QuizRepositoryImpl implements QuizRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<MemberQuizDto> findByMemberIdAndOrganizationId(Long memberId, Long orgId) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        MemberQuizDto.class,
                        quiz.id,
                        video.id,
                        video.title,
                        quiz.question,
                        quiz.description,
                        memberQuizResult.isCorrect,
                        quiz.answer
                ))
                .from(memberQuizResult)
                .join(memberQuizResult.quiz, quiz)
                .join(quiz.video, video)
                .where(
                        memberQuizResult.member.id.eq(memberId),
                        video.organization.id.eq(orgId)
                )
                .orderBy(memberQuizResult.submittedAt.desc())
                .fetch();
    }

    @Override
    public List<QuizInfo> findByVideoId(Long videoId) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        QuizInfo.class,
                        quiz.id,
                        quiz.question,
                        quiz.answer
                ))
                .from(quiz)
                .join(quiz.video, video)
                .where(
                        video.id.eq(videoId)
                                .and(video.status.eq(ACTIVE)))
                .fetch();
    }

}
