package app.allstackproject.privideo.repository.video.custom;

import static app.allstackproject.privideo.common.enumStatus.BaseStatusType.ACTIVE;
import static app.allstackproject.privideo.common.enumStatus.JoinStatusType.APPROVED;
import static app.allstackproject.privideo.entity.QMember.member;
import static app.allstackproject.privideo.entity.QMemberGroupMapping.memberGroupMapping;
import static app.allstackproject.privideo.entity.QVideo.video;
import static app.allstackproject.privideo.entity.QVideoMemberGroupMapping.videoMemberGroupMapping;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VideoRepositoryImpl implements VideoRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public boolean isValidMemberAndOrgAndVideo(Long memberId, Long orgId, Long videoId) {
        BooleanExpression openToAll = JPAExpressions.selectOne()
                .from(videoMemberGroupMapping)
                .where(videoMemberGroupMapping.video.id.eq(videoId),
                        videoMemberGroupMapping.status.eq(ACTIVE))
                .notExists();

        BooleanExpression memberGroupMatch = JPAExpressions.selectOne()
                .from(videoMemberGroupMapping)
                .join(memberGroupMapping)
                .on(memberGroupMapping.memberGroup.id.eq(videoMemberGroupMapping.memberGroup.id)
                        .and(memberGroupMapping.member.id.eq(memberId))
                        .and(memberGroupMapping.status.eq(ACTIVE)))
                .where(videoMemberGroupMapping.video.id.eq(videoId),
                        videoMemberGroupMapping.status.eq(ACTIVE))
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
                        video.status.eq(ACTIVE),
                        openToAll.or(memberGroupMatch)
                )
                .fetchFirst();

        return ok != null;
    }
}
