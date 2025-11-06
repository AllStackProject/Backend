package app.allstackproject.privideo.repository.comment;

import static app.allstackproject.privideo.common.enumStatus.BaseStatusType.ACTIVE;
import static app.allstackproject.privideo.common.enumStatus.JoinStatusType.APPROVED;
import static app.allstackproject.privideo.entity.QMember.member;
import static app.allstackproject.privideo.entity.QMemberGroupMapping.memberGroupMapping;
import static app.allstackproject.privideo.entity.QUser.user;
import static app.allstackproject.privideo.entity.QVideo.video;
import static app.allstackproject.privideo.entity.QVideoGroupAuthority.videoGroupAuthority;

import app.allstackproject.privideo.dto.video.CommentInfo;
import app.allstackproject.privideo.entity.QComment;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<CommentInfo> findAllByVideoId(Long videoId) {
        QComment c = new QComment("c");

        return jpaQueryFactory
                .select(Projections.constructor(CommentInfo.class, c.id, c.text, user.name, c.createdAt))
                .from(c)
                .join(c.video, video)
                .join(c.member, member)
                .join(member.user, user)
                .where(video.id.eq(videoId))
                .orderBy(c.createdAt.desc())
                .fetch();
    }

    @Override
    public boolean isValidMemberAndOrgAndVideo(Long memberId, Long orgId, Long videoId) {
        BooleanExpression vgaExists = JPAExpressions.selectOne()
                .from(videoGroupAuthority)
                .where(videoGroupAuthority.video.id.eq(videoId),
                        videoGroupAuthority.status.eq(ACTIVE))
                .exists();

        BooleanExpression vgaAndMemberMatch = JPAExpressions.selectOne()
                .from(videoGroupAuthority)
                .join(memberGroupMapping)
                .on(memberGroupMapping.memberGroup.id.eq(videoGroupAuthority.memberGroup.id)
                        .and(memberGroupMapping.member.id.eq(memberId))
                        .and(memberGroupMapping.status.eq(ACTIVE)))
                .where(videoGroupAuthority.video.id.eq(videoId),
                        videoGroupAuthority.status.eq(ACTIVE))
                .exists();

        Integer ok = jpaQueryFactory
                .selectOne()
                .from(video)
                .join(member).on(member.id.eq(memberId),
                        member.organization.id.eq(orgId),
                        member.status.eq(ACTIVE),
                        member.joinStatus.eq(APPROVED))
                .where(
                        video.id.eq(videoId),
                        video.organization.id.eq(orgId),
                        video.status.eq(ACTIVE),
                        vgaExists.not().or(vgaAndMemberMatch)
                )
                .fetchFirst();

        return ok != null;
    }
}
