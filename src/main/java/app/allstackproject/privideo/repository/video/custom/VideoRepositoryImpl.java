package app.allstackproject.privideo.repository.video.custom;

import static app.allstackproject.privideo.common.enumStatus.BaseStatusType.ACTIVE;
import static app.allstackproject.privideo.common.enumStatus.JoinStatusType.APPROVED;
import static app.allstackproject.privideo.entity.QHistory.history;
import static app.allstackproject.privideo.entity.QMember.member;
import static app.allstackproject.privideo.entity.QMemberGroupMapping.memberGroupMapping;
import static app.allstackproject.privideo.entity.QVideo.video;
import static app.allstackproject.privideo.entity.QVideoMemberGroupMapping.videoMemberGroupMapping;

import app.allstackproject.privideo.common.enumStatus.VideoOpenScopeType;
import app.allstackproject.privideo.dto.admin.QuitLogItem;
import app.allstackproject.privideo.dto.admin.ReadAllVideoIntervalLogItem;
import app.allstackproject.privideo.dto.admin.ReadAllVideoItem;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
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
                .then(VideoOpenScopeType.GROUP.name())
                .otherwise(VideoOpenScopeType.PUBLIC.name());

        return jpaQueryFactory
                .select(Projections.constructor(ReadAllVideoItem.class,
                        video.id,
                        video.title,
                        video.thumbnailUrl,
                        video.createdAt,
                        video.expiredAt,
                        openScope,
                        video.watchCnt
                ))
                .from(video)
                .where(video.organization.id.eq(orgId))
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
    public List<QuitLogItem> findTopQuitRateVideosByOrgId(Long orgId, int limit) {
        NumberExpression<Long> quitRate = new CaseBuilder()
                .when(video.watchCnt.eq(0L)).then(0L)
                .otherwise(video.quitCnt.multiply(100).divide(video.watchCnt));

        NumberExpression<Long> avgWatchTime = history.watchRate
                .multiply(video.wholeTime)
                .divide(100L)
                .avg()
                .longValue();

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
                        history.member.joinStatus.eq(APPROVED),
                        history.member.status.eq(ACTIVE)
                )
                .groupBy(
                        video.id,
                        video.title,
                        video.createdAt,
                        video.watchCnt,
                        video.quitCnt,
                        video.wholeTime
                )
                .orderBy(quitRate.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<QuitLogItem> findLowQuitRateVideosByOrgId(Long orgId, int limit) {
        NumberExpression<Long> quitRate = new CaseBuilder()
                .when(video.watchCnt.eq(0L)).then(0L)
                .otherwise(video.quitCnt.multiply(100).divide(video.watchCnt));

        NumberExpression<Long> avgWatchTime = history.watchRate
                .multiply(video.wholeTime)
                .divide(100L)
                .avg()
                .longValue();

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
                        history.member.joinStatus.eq(APPROVED),
                        history.member.status.eq(ACTIVE)
                )
                .groupBy(
                        video.id,
                        video.title,
                        video.createdAt,
                        video.watchCnt,
                        video.quitCnt,
                        video.wholeTime
                )
                .orderBy(quitRate.asc())
                .limit(limit)
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
                                        .where(videoMemberGroupMapping.video.id.eq(videoId))
                        )
                )
                .exists();

        return noGroupRestriction.or(memberInAllowedGroup);
    }
}
