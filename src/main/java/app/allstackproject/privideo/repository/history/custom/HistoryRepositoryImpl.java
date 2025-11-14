package app.allstackproject.privideo.repository.history.custom;

import static app.allstackproject.privideo.common.enumStatus.BaseStatusType.ACTIVE;
import static app.allstackproject.privideo.entity.QHistory.history;
import static app.allstackproject.privideo.entity.QScrap.scrap;
import static app.allstackproject.privideo.entity.QVideo.video;

import app.allstackproject.privideo.dto.admin.MemberWatchLogItem;
import app.allstackproject.privideo.dto.history.VideoHistory;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HistoryRepositoryImpl implements HistoryRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<VideoHistory> findByMemberId(Long memberId) {
        BooleanExpression scrappedExists = JPAExpressions
                .selectOne()
                .from(scrap)
                .where(
                        scrap.member.id.eq(memberId),
                        scrap.video.id.eq(history.video.id),
                        scrap.video.status.eq(ACTIVE)
                )
                .exists();

        return jpaQueryFactory
                .select(Projections.constructor(
                        VideoHistory.class,
                        video.id,
                        video.title,
                        video.thumbnailUrl,
                        history.watchRate,
                        history.lastModifiedAt,
                        video.wholeTime,
                        scrappedExists
                ))
                .from(history)
                .join(history.video, video)
                .where(
                        history.member.id.eq(memberId)
                )
                .orderBy(history.lastModifiedAt.desc())
                .fetch();
    }

    @Override
    public List<MemberWatchLogItem> findStatByMemberId(Long memberId) {
        DateTimeExpression<LocalDateTime> watchedAt = new CaseBuilder()
                .when(history.isComplete.isTrue())
                .then(history.completedAt)
                .otherwise(history.lastModifiedAt);

        return jpaQueryFactory
                .select(Projections.constructor(
                        MemberWatchLogItem.class,
                        video.id,
                        video.title,
                        history.watchRate,
                        watchedAt
                ))
                .from(history)
                .join(history.video, video)
                .where(
                        history.member.id.eq(memberId)
                )
                .orderBy(watchedAt.asc())
                .fetch();
    }
}
