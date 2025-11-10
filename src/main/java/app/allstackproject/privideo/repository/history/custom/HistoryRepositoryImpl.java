package app.allstackproject.privideo.repository.history.custom;

import static app.allstackproject.privideo.entity.QHistory.history;
import static app.allstackproject.privideo.entity.QScrap.scrap;
import static app.allstackproject.privideo.entity.QVideo.video;

import app.allstackproject.privideo.common.enumStatus.BaseStatusType;
import app.allstackproject.privideo.dto.history.VideoHistory;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HistoryRepositoryImpl implements HistoryRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<VideoHistory> findByMemberIdAndOrganizationId(Long memberId, Long orgId) {
        BooleanExpression scrappedExists = JPAExpressions
                .selectOne()
                .from(scrap)
                .where(
                        scrap.member.id.eq(memberId),
                        scrap.video.id.eq(history.video.id),
                        scrap.status.eq(BaseStatusType.ACTIVE)
                )
                .exists();

        return jpaQueryFactory
                .select(Projections.constructor(
                        VideoHistory.class,
                        video.id,
                        video.title,
                        video.thumbnailUrl,
                        history.watchRate,
                        history.recentPositionSec,
                        scrappedExists
                ))
                .from(history)
                .join(history.video, video)
                .where(
                        history.member.id.eq(memberId),
                        history.status.eq(BaseStatusType.ACTIVE),
                        video.organization.id.eq(orgId)
                )
                .orderBy(history.lastModifiedAt.desc())
                .fetch();
    }
}
