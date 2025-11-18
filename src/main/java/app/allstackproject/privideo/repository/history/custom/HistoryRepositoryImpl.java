package app.allstackproject.privideo.repository.history.custom;

import static app.allstackproject.privideo.common.enumStatus.BaseStatusType.ACTIVE;
import static app.allstackproject.privideo.common.enumStatus.JoinStatusType.APPROVED;
import static app.allstackproject.privideo.entity.QCategory.category;
import static app.allstackproject.privideo.entity.QHistory.history;
import static app.allstackproject.privideo.entity.QMember.member;
import static app.allstackproject.privideo.entity.QMemberGroup.memberGroup;
import static app.allstackproject.privideo.entity.QMemberGroupMapping.memberGroupMapping;
import static app.allstackproject.privideo.entity.QScrap.scrap;
import static app.allstackproject.privideo.entity.QVideo.video;
import static app.allstackproject.privideo.entity.QVideoCategoryMapping.videoCategoryMapping;
import static app.allstackproject.privideo.entity.QVideoMemberGroupMapping.videoMemberGroupMapping;

import app.allstackproject.privideo.common.enumStatus.VideoOpenScopeType;
import app.allstackproject.privideo.dto.admin.AllVideoWatchLogItem;
import app.allstackproject.privideo.dto.admin.GroupWatchCompleteRate;
import app.allstackproject.privideo.dto.admin.MemberAvgWatchRateDto;
import app.allstackproject.privideo.dto.admin.MemberWatchLogItem;
import app.allstackproject.privideo.dto.admin.MonthlyWatchItem;
import app.allstackproject.privideo.dto.admin.VideoWatchLogItem;
import app.allstackproject.privideo.dto.history.VideoHistory;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
                        scrap.video.id.eq(history.video.id)
                )
                .exists();

        return jpaQueryFactory
                .select(Projections.constructor(
                        VideoHistory.class,
                        video.id,
                        video.title,
                        video.thumbnailUrl,
                        history.watchRate,
                        history.lastWatchedAt,
                        video.wholeTime,
                        scrappedExists
                ))
                .from(history)
                .join(history.video, video)
                .where(
                        history.member.id.eq(memberId),
                        history.member.joinStatus.eq(APPROVED)
                )
                .orderBy(history.lastWatchedAt.desc())
                .fetch();
    }

    @Override
    public List<MemberWatchLogItem> findWatchLogByMemberId(Long memberId) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        MemberWatchLogItem.class,
                        video.id,
                        video.title,
                        history.watchRate,
                        history.lastWatchedAt
                ))
                .from(history)
                .join(history.video, video)
                .where(
                        history.member.id.eq(memberId),
                        history.member.joinStatus.eq(APPROVED)
                )
                .orderBy(history.lastWatchedAt.asc())
                .fetch();
    }

    @Override
    public List<MemberAvgWatchRateDto> findMemberAvgWatchRateByOrgId(Long orgId) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        MemberAvgWatchRateDto.class,
                        history.member.id,
                        history.watchRate.avg().longValue()
                ))
                .from(history)
                .join(history.member, member)
                .where(
                        member.organization.id.eq(orgId),
                        history.member.joinStatus.eq(APPROVED),
                        history.member.status.eq(ACTIVE)
                )
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
                        video.createdAt,
                        openScope,
                        completeRate,
                        history.member.id.countDistinct()
                ))
                .from(video)
                .join(video.creator, member)
                .leftJoin(history).on(history.video.id.eq(video.id))
                .where(
                        video.organization.id.eq(orgId),
                        history.member.joinStatus.eq(APPROVED),
                        history.member.status.eq(ACTIVE)
                )
                .groupBy(
                        video.id,
                        video.title,
                        member.nickname,
                        video.expiredAt,
                        openScope
                )
                .fetch();
    }

    @Override
    public List<VideoWatchLogItem> findVideoWatchLogByVideoId(Long videoId) {
        List<Tuple> historyData = jpaQueryFactory
                .select(
                        history.member.id,
                        member.nickname,
                        history.watchRate,
                        history.lastWatchedAt
                )
                .from(history)
                .join(history.member, member)
                .where(
                        history.video.id.eq(videoId),
                        history.member.joinStatus.eq(APPROVED),
                        history.member.status.eq(ACTIVE)
                )
                .orderBy(history.lastWatchedAt.desc())
                .fetch();

        if (historyData.isEmpty()) {
            return List.of();
        }

        List<Long> memberIds = historyData.stream()
                .map(t -> t.get(history.member.id))
                .distinct()
                .collect(Collectors.toList());

        Map<Long, List<String>> groupsMap = jpaQueryFactory
                .select(memberGroupMapping.member.id, memberGroup.name)
                .from(memberGroupMapping)
                .join(memberGroupMapping.memberGroup, memberGroup)
                .where(
                        memberGroupMapping.member.id.in(memberIds),
                        memberGroupMapping.status.eq(ACTIVE)
                )
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(
                        t -> t.get(memberGroupMapping.member.id),
                        Collectors.mapping(
                                t -> t.get(memberGroup.name),
                                Collectors.toList()
                        )
                ));

        return historyData.stream()
                .map(t -> new VideoWatchLogItem(
                        t.get(member.nickname),
                        groupsMap.getOrDefault(t.get(history.member.id), List.of()),
                        t.get(history.watchRate),
                        t.get(history.lastWatchedAt)
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> findTopCategoriesByMemberIdWithinPeriod(Long memberId, LocalDateTime startDate,
                                                                LocalDateTime endDate) {
        List<Tuple> rows = jpaQueryFactory
                .select(
                        category.title,
                        category.id.count()
                )
                .from(history)
                .join(history.video, video)
                .join(videoCategoryMapping).on(videoCategoryMapping.video.eq(video))
                .join(videoCategoryMapping.category, category)
                .where(
                        history.member.id.eq(memberId),
                        history.isComplete.isTrue(),
                        history.completedAt.between(startDate, endDate),
                        history.member.joinStatus.eq(APPROVED),
                        history.member.status.eq(ACTIVE)
                )
                .groupBy(category.id, category.title)
                .orderBy(category.id.count().desc())
                .limit(3)
                .fetch();

        return rows.stream()
                .map(t -> t.get(category.title))
                .collect(Collectors.toList());
    }

    @Override
    public List<MonthlyWatchItem> findMonthlyStatsByMemberIdWithinPeriod(Long memberId, LocalDateTime startDate,
                                                                         LocalDateTime endDate) {
        NumberExpression<Integer> yearExpr = history.completedAt.year();
        NumberExpression<Integer> monthExpr = history.completedAt.month();
        NumberExpression<Long> countExpr = history.id.countDistinct();

        var rows = jpaQueryFactory
                .select(yearExpr, monthExpr, countExpr)
                .from(history)
                .where(
                        history.member.id.eq(memberId),
                        history.isComplete.isTrue(),
                        history.completedAt.between(startDate, endDate),
                        history.member.joinStatus.eq(APPROVED),
                        history.member.status.eq(ACTIVE)
                )
                .groupBy(yearExpr, monthExpr)
                .orderBy(yearExpr.asc(), monthExpr.asc())
                .fetch();

        return rows.stream()
                .map(t -> {
                    return new MonthlyWatchItem(t.get(yearExpr), t.get(monthExpr), t.get(countExpr));
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupWatchCompleteRate> findGroupAvgWatchRateByOrgIdWithinPeriod(Long orgId, LocalDateTime startDate,
                                                                                 LocalDateTime endDate) {
        NumberExpression<Long> completeCount = new CaseBuilder()
                .when(history.isComplete.eq(true)).then(1L)
                .otherwise(0L)
                .sum();

        NumberExpression<Long> totalCount = history.count();

        NumberExpression<Long> completeRate = new CaseBuilder()
                .when(totalCount.eq(0L)).then(0L)
                .otherwise(completeCount.multiply(100).divide(totalCount));

        return jpaQueryFactory
                .select(Projections.constructor(GroupWatchCompleteRate.class,
                        memberGroup.name,
                        completeRate
                ))
                .from(memberGroup)
                .leftJoin(memberGroupMapping).on(
                        memberGroupMapping.memberGroup.id.eq(memberGroup.id),
                        memberGroupMapping.status.eq(ACTIVE)
                )
                .leftJoin(memberGroupMapping.member, member)
                .leftJoin(history).on(
                        history.member.id.eq(member.id),
                        history.startedAt.goe(startDate),
                        history.startedAt.lt(endDate)
                )
                .where(
                        memberGroup.organization.id.eq(orgId),
                        history.member.joinStatus.eq(APPROVED),
                        history.member.status.eq(ACTIVE)
                )
                .groupBy(memberGroup.id, memberGroup.name)
                .orderBy(memberGroup.name.asc())
                .fetch();
    }
}
