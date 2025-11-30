package app.allstackproject.privideo.domain.organization.repository.custom;

import static app.allstackproject.privideo.domain.member.entity.QMember.member;
import static app.allstackproject.privideo.domain.organization.entity.QOrganization.organization;
import static app.allstackproject.privideo.shared.enums.BaseStatusType.ACTIVE;

import app.allstackproject.privideo.domain.organization.dto.response.ReadOrgResult;
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
                        organization.imgKey,
                        member.createdAt,
                        member.isAdmin,
                        member.permissionCode,
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
