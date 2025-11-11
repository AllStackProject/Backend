package app.allstackproject.privideo.repository.member.custom;

import static app.allstackproject.privideo.common.enumStatus.BaseStatusType.ACTIVE;
import static app.allstackproject.privideo.common.enumStatus.BaseStatusType.INACTIVE;
import static app.allstackproject.privideo.entity.QMember.member;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public long inactivateAllByUserId(Long userId) {
        return jpaQueryFactory
                .update(member)
                .set(member.status, INACTIVE)
                .where(member.user.id.eq(userId)
                        .and(member.status.eq(ACTIVE)))
                .execute();
    }
}
