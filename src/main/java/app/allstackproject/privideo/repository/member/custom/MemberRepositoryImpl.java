package app.allstackproject.privideo.repository.member.custom;

import static app.allstackproject.privideo.common.enumStatus.BaseStatusType.ACTIVE;
import static app.allstackproject.privideo.common.enumStatus.BaseStatusType.INACTIVE;
import static app.allstackproject.privideo.common.enumStatus.JoinStatusType.APPROVED;
import static app.allstackproject.privideo.entity.QMember.member;
import static app.allstackproject.privideo.entity.QMemberGroupMapping.memberGroupMapping;
import static app.allstackproject.privideo.entity.QUser.user;

import app.allstackproject.privideo.common.enumStatus.JoinStatusType;
import app.allstackproject.privideo.dto.admin.AgeCountDto;
import app.allstackproject.privideo.dto.admin.GenderCountDto;
import app.allstackproject.privideo.dto.admin.MemberGroupItem;
import app.allstackproject.privideo.dto.admin.ReadAllJoinRequestItem;
import app.allstackproject.privideo.dto.admin.ReadAllMemberItem;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public long inactivateAllByUserId(Long userId) {
        return jpaQueryFactory
                .update(member)
                .set(member.status, INACTIVE)
                .where(
                        member.user.id.eq(userId),
                        member.status.eq(ACTIVE),
                        member.joinStatus.eq(APPROVED)
                )
                .execute();
    }

    @Override
    public List<ReadAllMemberItem> findByOrganizationId(Long orgId) {
        List<ReadAllMemberItem> members = jpaQueryFactory
                .select(Projections.constructor(ReadAllMemberItem.class,
                        member.id,
                        user.name,
                        member.nickname,
                        member.isAdmin,
                        member.permissionCode.gt(0L)
                ))
                .from(member)
                .join(member.user, user)
                .where(
                        member.organization.id.eq(orgId),
                        member.joinStatus.eq(APPROVED),
                        member.status.eq(ACTIVE)
                )
                .fetch();

        if (!members.isEmpty()) {
            List<Long> memberIds = members.stream()
                    .map(ReadAllMemberItem::getId)
                    .collect(Collectors.toList());

            Map<Long, List<MemberGroupItem>> memberGroupMap = jpaQueryFactory
                    .select(
                            memberGroupMapping.member.id,
                            memberGroupMapping.memberGroup.id,
                            memberGroupMapping.memberGroup.name
                    )
                    .from(memberGroupMapping)
                    .where(memberGroupMapping.member.id.in(memberIds))
                    .fetch()
                    .stream()
                    .collect(Collectors.groupingBy(
                            tuple -> tuple.get(memberGroupMapping.member.id),
                            Collectors.mapping(
                                    tuple -> new MemberGroupItem(
                                            tuple.get(memberGroupMapping.memberGroup.id),
                                            tuple.get(memberGroupMapping.memberGroup.name)
                                    ),
                                    Collectors.toList()
                            )
                    ));

            members.forEach(dto ->
                    dto.setMemberGroups(memberGroupMap.getOrDefault(dto.getId(), Collections.emptyList()))
            );
        }

        return members;
    }

    @Override
    public List<ReadAllJoinRequestItem> findByOrganizationIdAndJoinStatus(Long orgId, JoinStatusType joinStatus) {
        return jpaQueryFactory
                .select(Projections.constructor(ReadAllJoinRequestItem.class,
                        member.id,
                        user.name,
                        member.nickname,
                        member.createdAt
                ))
                .from(member)
                .join(member.user, user)
                .where(
                        member.organization.id.eq(orgId),
                        member.status.eq(ACTIVE),
                        member.joinStatus.eq(joinStatus)
                )
                .fetch();
    }

    @Override
    public List<GenderCountDto> countMemberByGender(Long orgId) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        GenderCountDto.class,
                        user.gender,
                        member.count()
                ))
                .from(member)
                .join(member.user, user)
                .where(
                        user.status.eq(ACTIVE),
                        member.organization.id.eq(orgId),
                        member.status.eq(ACTIVE),
                        member.joinStatus.eq(JoinStatusType.APPROVED)
                )
                .groupBy(user.gender)
                .fetch();
    }

    @Override
    public List<AgeCountDto> countMemberByAge(Long orgId) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        AgeCountDto.class,
                        user.age,
                        member.count()
                ))
                .from(member)
                .join(member.user, user)
                .where(
                        user.status.eq(ACTIVE),
                        member.organization.id.eq(orgId),
                        member.status.eq(ACTIVE),
                        member.joinStatus.eq(JoinStatusType.APPROVED)
                )
                .groupBy(user.age)
                .fetch();
    }
}
