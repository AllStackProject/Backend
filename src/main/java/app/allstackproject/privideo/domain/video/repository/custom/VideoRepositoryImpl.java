package app.allstackproject.privideo.domain.video.repository.custom;

import static app.allstackproject.privideo.domain.history.entity.QHistory.history;
import static app.allstackproject.privideo.domain.member.entity.QMember.member;
import static app.allstackproject.privideo.domain.member.entity.QMemberGroupMapping.memberGroupMapping;
import static app.allstackproject.privideo.domain.organization.dto.enums.JoinStatusType.APPROVED;
import static app.allstackproject.privideo.domain.scrap.entity.QScrap.scrap;
import static app.allstackproject.privideo.domain.video.entity.QCategory.category;
import static app.allstackproject.privideo.domain.video.entity.QVideo.video;
import static app.allstackproject.privideo.domain.video.entity.QVideoCategoryMapping.videoCategoryMapping;
import static app.allstackproject.privideo.domain.video.entity.QVideoMemberGroupMapping.videoMemberGroupMapping;
import static app.allstackproject.privideo.domain.video.enums.UploadStatusType.COMPLETE;
import static app.allstackproject.privideo.shared.enums.BaseStatusType.ACTIVE;

import app.allstackproject.privideo.domain.admin.dto.QuitLogItem;
import app.allstackproject.privideo.domain.admin.dto.ReadAllVideoIntervalLogItem;
import app.allstackproject.privideo.domain.admin.dto.ReadAllVideoItem;
import app.allstackproject.privideo.domain.admin.dto.VideoRankItem;
import app.allstackproject.privideo.domain.organization.dto.enums.OpenScopeType;
import app.allstackproject.privideo.domain.video.enums.FilterType;
import app.allstackproject.privideo.dto.home.HomeVideoItem;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
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
public class VideoRepositoryImpl implements VideoRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public boolean isValidMemberAndOrgAndVideo(Long memberId, Long orgId, Long videoId) {
        Integer result = jpaQueryFactory
                .selectOne()
                .from(video)
                .join(member).on(member.id.eq(memberId))
                .where(
                        video.id.eq(videoId),
                        video.organization.id.eq(orgId),
                        video.uploadStatus.eq(COMPLETE),
                        member.organization.id.eq(orgId),
                        member.status.eq(ACTIVE),
                        member.joinStatus.eq(APPROVED),
                        isVideoAccessibleByMember(videoId, memberId)
                )
                .fetchFirst();

        return result != null;
    }

    @Override
    public List<ReadAllVideoItem> findByOrgId(Long orgId) {
        StringExpression openScope = new CaseBuilder()
                .when(JPAExpressions
                        .selectOne()
                        .from(videoMemberGroupMapping)
                        .where(videoMemberGroupMapping.video.id.eq(video.id))
                        .exists())
                .then(OpenScopeType.GROUP.name())
                .otherwise(OpenScopeType.PUBLIC.name());

        return jpaQueryFactory
                .select(Projections.constructor(ReadAllVideoItem.class,
                        video.id,
                        video.title,
                        video.thumbnailKey,
                        video.createdAt,
                        video.expiredAt,
                        openScope,
                        video.watchCnt
                ))
                .from(video)
                .where(
                        video.organization.id.eq(orgId),
                        video.uploadStatus.eq(COMPLETE),
                        video.creator.status.eq(ACTIVE),
                        video.creator.joinStatus.eq(APPROVED)
                )
                .fetch();
    }

    @Override
    public List<ReadAllVideoItem> findByOrgIdAndCreatorId(Long orgId, Long memberId) {
        StringExpression openScope = new CaseBuilder()
                .when(JPAExpressions
                        .selectOne()
                        .from(videoMemberGroupMapping)
                        .where(videoMemberGroupMapping.video.id.eq(video.id))
                        .exists())
                .then(OpenScopeType.GROUP.name())
                .otherwise(OpenScopeType.PUBLIC.name());

        return jpaQueryFactory
                .select(Projections.constructor(ReadAllVideoItem.class,
                        video.id,
                        video.title,
                        video.thumbnailKey,
                        video.createdAt,
                        video.expiredAt,
                        openScope,
                        video.watchCnt
                ))
                .from(video)
                .where(
                        video.organization.id.eq(orgId),
                        video.uploadStatus.eq(COMPLETE),
                        video.creator.id.eq(memberId),
                        video.creator.status.eq(ACTIVE),
                        video.creator.joinStatus.eq(APPROVED)
                )
                .fetch();
    }

    @Override
    public List<ReadAllVideoIntervalLogItem> findAllVideoIntervalLogByOrgId(Long orgId) {
        NumberExpression<Long> quitRate = new CaseBuilder()
                .when(video.watchCnt.eq(0L)).then(0L)
                .otherwise(video.quitCnt.multiply(100).divide(video.watchCnt));

        return jpaQueryFactory
                .select(Projections.constructor(ReadAllVideoIntervalLogItem.class,
                        video.id,
                        video.title,
                        member.nickname,
                        history.member.id.countDistinct(),
                        quitRate
                ))
                .from(video)
                .join(video.creator, member)
                .leftJoin(history).on(history.video.id.eq(video.id))
                .where(
                        video.organization.id.eq(orgId),
                        video.uploadStatus.eq(COMPLETE),
                        member.joinStatus.eq(APPROVED),
                        member.status.eq(ACTIVE)
                )
                .groupBy(
                        video.id,
                        video.title,
                        member.nickname,
                        video.watchCnt,
                        video.quitCnt
                )
                .orderBy(video.createdAt.desc())
                .fetch();
    }

    @Override
    public List<VideoRankItem> findTop5VideoRankByOrgId(Long orgId) {
        List<Tuple> results = jpaQueryFactory
                .select(
                        video.title,
                        video.createdAt,
                        video.watchCnt.as("watchCnt"),
                        history.isComplete.when(true).then(1L).otherwise(0L).sum().as("completeCnt")
                )
                .from(video)
                .leftJoin(history).on(history.video.id.eq(video.id))
                .where(
                        video.organization.id.eq(orgId),
                        video.uploadStatus.eq(COMPLETE),
                        video.creator.status.eq(ACTIVE),
                        video.creator.joinStatus.eq(APPROVED)
                )
                .groupBy(video.id, video.title, video.createdAt)
                .orderBy(
                        video.watchCnt.desc(),
                        history.isComplete.when(true).then(1L).otherwise(0L).sum().desc()
                )
                .limit(5)
                .fetch();

        return results.stream()
                .map(tuple -> {
                    Long watchCnt = tuple.get(2, Long.class);
                    Long completeCnt = tuple.get(3, Long.class);
                    Long watchCompleteRate = watchCnt > 0 ? (completeCnt * 100 / watchCnt) : 0L;

                    return new VideoRankItem(
                            tuple.get(0, String.class),
                            tuple.get(1, LocalDateTime.class),
                            watchCnt,
                            watchCompleteRate
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<QuitLogItem> findTopQuitRateVideosByOrgId(Long orgId, int limit) {
        return findQuitRateVideosByOrgId(orgId, limit, true);
    }

    @Override
    public List<QuitLogItem> findLowQuitRateVideosByOrgId(Long orgId, int limit) {
        return findQuitRateVideosByOrgId(orgId, limit, false);
    }

    @Override
    public List<HomeVideoItem> findHomeVideos(Long orgId, Long memberId, FilterType filter) {
        BooleanExpression scrappedExists = JPAExpressions
                .selectOne()
                .from(scrap)
                .where(
                        scrap.member.id.eq(memberId),
                        scrap.video.id.eq(video.id)
                )
                .exists();

        OrderSpecifier<?> orderBy;
        switch (filter) {
            case RECENT -> orderBy = video.createdAt.desc();
            case POPULAR -> orderBy = video.watchCnt.desc();
            case RECOMMEND -> orderBy = video.watchCnt.asc();
            default -> orderBy = video.createdAt.desc();
        }

        return jpaQueryFactory
                .select(Projections.constructor(
                        HomeVideoItem.class,
                        video.id,
                        video.title,
                        video.thumbnailKey,
                        video.creator.nickname,
                        video.wholeTime,
                        video.watchCnt,
                        video.createdAt,
                        scrappedExists
                ))
                .from(video)
                .leftJoin(videoMemberGroupMapping)
                .on(videoMemberGroupMapping.video.eq(video))
                .leftJoin(memberGroupMapping)
                .on(
                        memberGroupMapping.memberGroup.eq(videoMemberGroupMapping.memberGroup),
                        memberGroupMapping.member.id.eq(memberId),
                        video.creator.joinStatus.eq(APPROVED),
                        video.creator.status.eq(ACTIVE)
                )
                .where(
                        video.organization.id.eq(orgId),
                        video.uploadStatus.eq(COMPLETE),
                        videoMemberGroupMapping.id.isNull()
                                .or(memberGroupMapping.id.isNotNull())
                )
                .groupBy(
                        video.id,
                        video.title,
                        video.thumbnailKey,
                        video.creator.nickname,
                        video.watchCnt,
                        video.createdAt
                )
                .orderBy(orderBy)
                .fetch();
    }

    @Override
    public Map<Long, List<String>> findCategoriesForHomeVideos(Long memberId, List<Long> videoIds) {
        if (videoIds.isEmpty()) {
            return Map.of();
        }

        List<Tuple> rows = jpaQueryFactory
                .select(
                        video.id,
                        category.title
                )
                .from(video)
                .join(videoCategoryMapping).on(videoCategoryMapping.video.eq(video))
                .join(category).on(videoCategoryMapping.category.eq(category))
                .leftJoin(videoMemberGroupMapping)
                .on(videoMemberGroupMapping.video.eq(video))
                .leftJoin(memberGroupMapping)
                .on(
                        memberGroupMapping.memberGroup.eq(videoMemberGroupMapping.memberGroup),
                        memberGroupMapping.member.id.eq(memberId),
                        video.creator.joinStatus.eq(APPROVED),
                        video.creator.status.eq(ACTIVE)
                )
                .where(
                        video.id.in(videoIds),
                        video.uploadStatus.eq(COMPLETE),
                        videoMemberGroupMapping.id.isNotNull(),
                        memberGroupMapping.id.isNotNull()
                )
                .fetch();

        return rows.stream()
                .collect(Collectors.groupingBy(
                        t -> t.get(video.id),
                        Collectors.mapping(
                                t -> t.get(category.title),
                                Collectors.toList()
                        )
                ));
    }

    @Override
    public List<HomeVideoItem> findSearchVideos(Long orgId, Long memberId, String keyword) {
        BooleanExpression scrappedExists = JPAExpressions
                .selectOne()
                .from(scrap)
                .where(
                        scrap.member.id.eq(memberId),
                        scrap.video.id.eq(video.id)
                )
                .exists();

        return jpaQueryFactory
                .select(Projections.constructor(
                        HomeVideoItem.class,
                        video.id,
                        video.title,
                        video.thumbnailKey,
                        video.creator.nickname,
                        video.wholeTime,
                        video.watchCnt,
                        video.createdAt,
                        scrappedExists
                ))
                .from(video)
                .leftJoin(videoMemberGroupMapping)
                .on(
                        videoMemberGroupMapping.video.eq(video),
                        video.creator.joinStatus.eq(APPROVED)
                )
                .leftJoin(memberGroupMapping)
                .on(
                        memberGroupMapping.memberGroup.eq(videoMemberGroupMapping.memberGroup),
                        memberGroupMapping.member.id.eq(memberId)
                )
                .where(
                        video.organization.id.eq(orgId),
                        video.title.containsIgnoreCase(keyword),
                        video.uploadStatus.eq(COMPLETE),
                        videoMemberGroupMapping.id.isNull()
                                .or(memberGroupMapping.id.isNotNull())
                )
                .groupBy(
                        video.id,
                        video.title,
                        video.thumbnailKey,
                        video.creator.nickname,
                        video.wholeTime,
                        video.watchCnt,
                        video.createdAt
                )
                .orderBy(video.createdAt.desc())
                .fetch();
    }

    /**
     * 비디오에 대한 멤버의 접근 권한 확인
     * - VideoMemberGroupMapping이 없으면 전체 공개 (OK)
     * - VideoMemberGroupMapping이 있으면 멤버가 해당 그룹에 속해야 함
     */
    private BooleanExpression isVideoAccessibleByMember(Long videoId, Long memberId) {
        BooleanExpression noGroupRestriction = JPAExpressions
                .selectOne()
                .from(videoMemberGroupMapping)
                .where(videoMemberGroupMapping.video.id.eq(videoId))
                .notExists();

        BooleanExpression memberInAllowedGroup = JPAExpressions
                .select(memberGroupMapping.memberGroup.id)
                .from(memberGroupMapping)
                .where(
                        memberGroupMapping.member.id.eq(memberId),
                        memberGroupMapping.member.joinStatus.eq(APPROVED),
                        memberGroupMapping.member.status.eq(ACTIVE),
                        memberGroupMapping.memberGroup.id.in(
                                JPAExpressions
                                        .select(videoMemberGroupMapping.memberGroup.id)
                                        .from(videoMemberGroupMapping)
                                        .where(
                                                videoMemberGroupMapping.video.id.eq(videoId),
                                                videoMemberGroupMapping.video.uploadStatus.eq(COMPLETE)
                                        )
                        )
                )
                .exists();

        return noGroupRestriction.or(memberInAllowedGroup);
    }

    private List<QuitLogItem> findQuitRateVideosByOrgId(Long orgId, int limit, boolean isHighQuitRate) {
        NumberExpression<Long> quitRate = new CaseBuilder()
                .when(video.watchCnt.eq(0L)).then(0L)
                .otherwise(video.quitCnt.multiply(100).divide(video.watchCnt));

        NumberExpression<Long> avgWatchTime = history.watchRate
                .multiply(video.wholeTime)
                .divide(100L)
                .avg()
                .longValue();

        BooleanExpression quitRateCondition = isHighQuitRate
                ? quitRate.goe(90L)
                : quitRate.lt(90L);

        OrderSpecifier<Long> orderCondition = isHighQuitRate
                ? quitRate.desc()
                : quitRate.asc();

        return jpaQueryFactory
                .select(Projections.constructor(QuitLogItem.class,
                        video.title,
                        video.createdAt,
                        avgWatchTime.coalesce(0L),
                        quitRate
                ))
                .from(video)
                .leftJoin(history).on(history.video.id.eq(video.id))
                .where(
                        video.organization.id.eq(orgId),
                        video.watchCnt.gt(0L),
                        video.uploadStatus.eq(COMPLETE),
                        video.creator.joinStatus.eq(APPROVED),
                        video.creator.status.eq(ACTIVE),
                        quitRateCondition
                )
                .groupBy(
                        video.id,
                        video.title,
                        video.createdAt,
                        video.watchCnt,
                        video.quitCnt,
                        video.wholeTime
                )
                .orderBy(orderCondition)
                .limit(limit)
                .fetch();
    }
}
