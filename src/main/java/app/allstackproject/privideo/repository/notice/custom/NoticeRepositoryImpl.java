package app.allstackproject.privideo.repository.notice.custom;

import static app.allstackproject.privideo.common.enumStatus.BaseStatusType.ACTIVE;
import static app.allstackproject.privideo.common.enumStatus.JoinStatusType.APPROVED;
import static app.allstackproject.privideo.entity.QMember.member;
import static app.allstackproject.privideo.entity.QNotice.notice;
import static app.allstackproject.privideo.entity.QNoticeMemberGroupMapping.noticeMemberGroupMapping;
import static app.allstackproject.privideo.entity.QVideo.video;
import static app.allstackproject.privideo.entity.QVideoMemberGroupMapping.videoMemberGroupMapping;

import app.allstackproject.privideo.common.enumStatus.OpenScopeType;
import app.allstackproject.privideo.dto.admin.ReadAllNotificationItem;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NoticeRepositoryImpl implements NoticeRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<ReadAllNotificationItem> findAllByOrganizationId(Long orgId) {
        StringExpression openScope = new CaseBuilder()
                .when(JPAExpressions
                        .selectOne()
                        .from(noticeMemberGroupMapping)
                        .where(noticeMemberGroupMapping.notice.id.eq(notice.id))
                        .exists())
                .then(OpenScopeType.GROUP.name())
                .otherwise(OpenScopeType.PUBLIC.name());

        return jpaQueryFactory.select(Projections.constructor(ReadAllNotificationItem.class,
                        notice.id,
                        notice.title,
                        member.nickname,
                        notice.createdAt,
                        notice.watchCnt,
                        openScope
                ))
                .from(notice)
                .join(notice.creator, member)
                .where(
                        notice.organization.id.eq(orgId),
                        member.joinStatus.eq(APPROVED),
                        member.status.eq(ACTIVE)
                )
                .fetch();
    }
}
