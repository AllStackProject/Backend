package app.allstackproject.privideo.domain.scrap.repository.custom;

import static app.allstackproject.privideo.domain.history.entity.QHistory.history;
import static app.allstackproject.privideo.domain.member.entity.QMember.member;
import static app.allstackproject.privideo.domain.member.entity.QMemberGroupMapping.memberGroupMapping;
import static app.allstackproject.privideo.domain.organization.dto.enums.JoinStatusType.APPROVED;
import static app.allstackproject.privideo.domain.scrap.entity.QScrap.scrap;
import static app.allstackproject.privideo.domain.video.entity.QVideo.video;
import static app.allstackproject.privideo.domain.video.entity.QVideoMemberGroupMapping.videoMemberGroupMapping;
import static app.allstackproject.privideo.domain.video.enums.UploadStatusType.COMPLETE;
import static app.allstackproject.privideo.shared.enums.BaseStatusType.ACTIVE;

import app.allstackproject.privideo.domain.history.dto.HistoryItem;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ScrapRepositoryImpl implements ScrapRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public boolean isValidMemberAndOrgAndVideo(Long memberId, Long orgId, Long videoId) {
        BooleanExpression openToAll = JPAExpressions.selectOne()
                .from(videoMemberGroupMapping)
                .where(videoMemberGroupMapping.video.id.eq(videoId))
                .notExists();

        BooleanExpression memberGroupMatch = JPAExpressions.selectOne()
                .from(videoMemberGroupMapping)
                .join(memberGroupMapping)
                .on(
                        memberGroupMapping.memberGroup.id.eq(videoMemberGroupMapping.memberGroup.id),
                        memberGroupMapping.member.id.eq(memberId)
                )
                .where(videoMemberGroupMapping.video.id.eq(videoId))
                .exists();

        Integer ok = jpaQueryFactory
                .selectOne()
                .from(video)
                .join(member).on(
                        member.id.eq(memberId),
                        member.organization.id.eq(orgId),
                        member.status.eq(ACTIVE),
                        member.joinStatus.eq(APPROVED))
                .where(
                        video.id.eq(videoId),
                        video.organization.id.eq(orgId),
                        video.uploadStatus.eq(COMPLETE),
                        openToAll.or(memberGroupMatch)
                )
                .fetchFirst();

        return ok != null;
    }

    @Override
    public int deleteByMemberIdAndVideoId(Long memberId, Long videoId) {
        return (int) jpaQueryFactory
                .delete(scrap)
                .where(
                        scrap.member.id.eq(memberId),
                        scrap.video.id.eq(videoId),
                        scrap.video.uploadStatus.eq(COMPLETE)
                )
                .execute();
    }

    @Override
    public List<HistoryItem> findByMemberIdAndOrganizationId(Long memberId, Long orgId) {
        BooleanExpression scrappedExists = JPAExpressions
                .selectOne()
                .from(scrap)
                .where(
                        scrap.member.id.eq(memberId),
                        scrap.video.id.eq(history.video.id),
                        scrap.video.uploadStatus.eq(COMPLETE)
                )
                .exists();

        return jpaQueryFactory
                .select(Projections.constructor(
                        HistoryItem.class,
                        video.id,
                        video.title,
                        video.thumbnailKey,
                        history.watchRate,
                        history.lastWatchedAt,
                        video.wholeTime
                ))
                .from(history)
                .join(history.video, video)
                .where(
                        history.member.id.eq(memberId),
                        history.member.status.eq(ACTIVE),
                        history.member.joinStatus.eq(APPROVED),
                        video.organization.id.eq(orgId),
                        scrappedExists
                )
                .orderBy(history.lastWatchedAt.desc())
                .fetch();
    }
}
