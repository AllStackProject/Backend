package app.allstackproject.privideo.repository.organization.custom;

import static app.allstackproject.privideo.common.enumStatus.BaseStatusType.ACTIVE;
import static app.allstackproject.privideo.entity.QMember.member;
import static app.allstackproject.privideo.entity.QOrganization.organization;

import app.allstackproject.privideo.dto.organization.ReadOrgResult;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrganizationRepositoryImpl implements OrganizationRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<ReadOrgResult> findAllByUserId(Long userId) {
        return jpaQueryFactory
                .select(Projections.constructor(ReadOrgResult.class,
                        organization.id,
                        organization.name,
                        organization.imgUrl,
                        member.createdAt,
                        member.isAdmin,
                        member.permissionCode.gt(0L),
                        member.joinStatus
                ))
                .from(member)
                .join(member.organization, organization)
                .where(
                        member.user.id.eq(userId),
                        member.status.eq(ACTIVE),
                        organization.status.eq(ACTIVE)
                )
                .fetch();
    }
}
