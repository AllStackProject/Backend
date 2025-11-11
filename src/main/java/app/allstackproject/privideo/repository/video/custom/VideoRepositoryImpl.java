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
        Integer result = jpaQueryFactory
                .selectOne()
                .from(video)
                .join(member).on(member.id.eq(memberId))
                .where(
                        video.id.eq(videoId),
                        video.organization.id.eq(orgId),
                        video.status.eq(ACTIVE),

                        member.organization.id.eq(orgId),
                        member.status.eq(ACTIVE),
                        member.joinStatus.eq(APPROVED),

                        isVideoAccessibleByMember(videoId, memberId)
                )
                .fetchFirst();

        return result != null;
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
                .where(
                        videoMemberGroupMapping.video.id.eq(videoId),
                        videoMemberGroupMapping.status.eq(ACTIVE)
                )
                .notExists();

        BooleanExpression memberInAllowedGroup = JPAExpressions
                .select(memberGroupMapping.memberGroup.id)
                .from(memberGroupMapping)
                .where(
                        memberGroupMapping.member.id.eq(memberId),
                        memberGroupMapping.status.eq(ACTIVE),
                        memberGroupMapping.member.joinStatus.eq(APPROVED),
                        memberGroupMapping.memberGroup.id.in(
                                JPAExpressions
                                        .select(videoMemberGroupMapping.memberGroup.id)
                                        .from(videoMemberGroupMapping)
                                        .where(
                                                videoMemberGroupMapping.video.id.eq(videoId),
                                                videoMemberGroupMapping.status.eq(ACTIVE)
                                        )
                        )
                )
                .exists();

        return noGroupRestriction.or(memberInAllowedGroup);
    }
}
