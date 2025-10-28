package app.allstackproject.privideo.repository.organization.custom;

import static app.allstackproject.privideo.entity.QMember.member;
import static app.allstackproject.privideo.entity.QOrganization.organization;

import app.allstackproject.privideo.dto.organization.QReadOrgDto;
import app.allstackproject.privideo.dto.organization.ReadOrgDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrganizationRepositoryImpl implements OrganizationRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<ReadOrgDto> findAllByUserId(Long userId) {
        return jpaQueryFactory
                .select(new QReadOrgDto(
                        organization.id,
                        organization.name,
                        organization.imgUrl,
                        organization.code,
                        member.createdAt,
                        member.isAdmin,
                        member.isApproved
                ))
                .from(member)
                .join(member.organization, organization)
                .where(member.user.id.eq(userId))
                .fetch();
    }
}
