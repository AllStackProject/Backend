package app.allstackproject.privideo.domain.notice.repository.custom;

import static app.allstackproject.privideo.domain.member.entity.QMember.member;
import static app.allstackproject.privideo.domain.member.entity.QMemberGroupMapping.memberGroupMapping;
import static app.allstackproject.privideo.domain.notice.entity.QNotice.notice;
import static app.allstackproject.privideo.domain.notice.entity.QNoticeMemberGroupMapping.noticeMemberGroupMapping;
import static app.allstackproject.privideo.domain.organization.dto.enums.JoinStatusType.APPROVED;
import static app.allstackproject.privideo.shared.enums.BaseStatusType.ACTIVE;

import app.allstackproject.privideo.domain.admin.dto.AdminReadAllNoticeItem;
import app.allstackproject.privideo.domain.organization.dto.enums.OpenScopeType;
import app.allstackproject.privideo.dto.home.ReadAllNoticeItem;
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
    public List<AdminReadAllNoticeItem> findAllByOrganizationId(Long orgId) {
        StringExpression openScope = new CaseBuilder()
                .when(JPAExpressions
                        .selectOne()
                        .from(noticeMemberGroupMapping)
                        .where(noticeMemberGroupMapping.notice.id.eq(notice.id))
                        .exists())
                .then(OpenScopeType.GROUP.name())
                .otherwise(OpenScopeType.PUBLIC.name());

        return jpaQueryFactory.select(Projections.constructor(AdminReadAllNoticeItem.class,
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

    @Override
    public List<ReadAllNoticeItem> findAllVisibleByOrgIdAndMemberId(Long orgId, Long memberId) {
        return jpaQueryFactory
                .selectDistinct(Projections.constructor(
                        ReadAllNoticeItem.class,
                        notice.id,
                        notice.title,
                        notice.createdAt,
                        notice.watchCnt
                ))
                .from(notice)
                .leftJoin(noticeMemberGroupMapping)
                .on(noticeMemberGroupMapping.notice.eq(notice))
                .leftJoin(memberGroupMapping)
                .on(
                        memberGroupMapping.memberGroup.eq(noticeMemberGroupMapping.memberGroup),
                        memberGroupMapping.member.id.eq(memberId)
                )
                .where(
                        notice.organization.id.eq(orgId),
                        noticeMemberGroupMapping.id.isNull()
                                .or(
                                        memberGroupMapping.id.isNotNull()
                                                .and(memberGroupMapping.member.status.eq(ACTIVE))
                                                .and(memberGroupMapping.member.joinStatus.eq(APPROVED))
                                )
                )
                .orderBy(notice.createdAt.desc())
                .fetch();
    }
}
