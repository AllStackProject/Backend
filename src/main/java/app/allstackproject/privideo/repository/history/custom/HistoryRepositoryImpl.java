package app.allstackproject.privideo.repository.history.custom;

import static app.allstackproject.privideo.common.enumStatus.BaseStatusType.ACTIVE;
import static app.allstackproject.privideo.entity.QHistory.history;
import static app.allstackproject.privideo.entity.QMember.member;
import static app.allstackproject.privideo.entity.QMemberGroup.memberGroup;
import static app.allstackproject.privideo.entity.QMemberGroupMapping.memberGroupMapping;
import static app.allstackproject.privideo.entity.QScrap.scrap;
import static app.allstackproject.privideo.entity.QVideo.video;
import static app.allstackproject.privideo.entity.QVideoMemberGroupMapping.videoMemberGroupMapping;

import app.allstackproject.privideo.common.enumStatus.VideoOpenScopeType;
import app.allstackproject.privideo.dto.admin.AllVideoWatchLogItem;
import app.allstackproject.privideo.dto.admin.MemberAvgWatchRateDto;
import app.allstackproject.privideo.dto.admin.MemberWatchLogItem;
import app.allstackproject.privideo.dto.admin.VideoWatchLogItem;
import app.allstackproject.privideo.dto.history.VideoHistory;
import com.querydsl.core.Tuple;
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
                        // history.lastModifiedAt // 최근 시청일 반환한다면
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

    @Override
    public List<VideoWatchLogItem> findVideoWatchLogByVideoId(Long videoId) {
        List<Tuple> historyData = jpaQueryFactory
                .select(
                        member.nickname,
                        history.watchRate,
                        history.startedAt
                )
                .from(history)
                .join(history.member, member)
                .where(history.video.id.eq(videoId))
                .orderBy(history.startedAt.desc())
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
                        t.get(history.lastModifiedAt)
                ))
                .collect(Collectors.toList());
    }
}
