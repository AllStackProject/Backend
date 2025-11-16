package app.allstackproject.privideo.repository.history.custom;

import static app.allstackproject.privideo.common.enumStatus.BaseStatusType.ACTIVE;
import static app.allstackproject.privideo.entity.QHistory.history;
import static app.allstackproject.privideo.entity.QMember.member;
import static app.allstackproject.privideo.entity.QScrap.scrap;
import static app.allstackproject.privideo.entity.QVideo.video;
import static app.allstackproject.privideo.entity.QVideoMemberGroupMapping.videoMemberGroupMapping;

import app.allstackproject.privideo.common.enumStatus.VideoOpenScopeType;
import app.allstackproject.privideo.dto.admin.AllVideoWatchLogItem;
import app.allstackproject.privideo.dto.admin.MemberAvgWatchRateDto;
import app.allstackproject.privideo.dto.admin.MemberWatchLogItem;
import app.allstackproject.privideo.dto.history.VideoHistory;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
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
    public List<MemberWatchLogItem> findWatchLogByMemberId(Long memberId) {
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

    @Override
    public List<MemberAvgWatchRateDto> findAvgWatchRateByOrgId(Long orgId) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        MemberAvgWatchRateDto.class,
                        history.member.id,
                        history.watchRate.avg().longValue()
                ))
                .from(history)
                .join(history.member, member)
                .where(member.organization.id.eq(orgId))
                .groupBy(history.member.id)
                .fetch();
    }

    @Override
    public List<AllVideoWatchLogItem> findAllVideoWatchLogByOrgId(Long orgId) {
        StringExpression openScope = new CaseBuilder()
                .when(JPAExpressions
                        .selectOne()
                        .from(videoMemberGroupMapping)
                        .where(
                                videoMemberGroupMapping.video.id.eq(video.id),
                                videoMemberGroupMapping.status.eq(ACTIVE)
                        )
                        .exists())
                .then(VideoOpenScopeType.GROUP.name())
                .otherwise(VideoOpenScopeType.PUBLIC.name());

        NumberExpression<Long> completeCount = new CaseBuilder()
                .when(history.isComplete.eq(true)).then(1L)
                .otherwise(0L)
                .sum();

        NumberExpression<Long> totalCount = history.count();

        NumberExpression<Long> completeRate = new CaseBuilder()
                .when(totalCount.eq(0L)).then(0L)
                .otherwise(completeCount.multiply(100).divide(totalCount));

        return jpaQueryFactory
                .select(Projections.constructor(AllVideoWatchLogItem.class,
                        video.id,
                        video.title,
                        member.nickname,
                        video.expiredAt,
                        openScope,
                        completeRate,
                        history.member.id.countDistinct()
                ))
                .from(video)
                .join(video.creator, member)
                .leftJoin(history).on(history.video.id.eq(video.id))
                .where(video.organization.id.eq(orgId))
                .groupBy(video.id)
                .fetch();
    }
}
