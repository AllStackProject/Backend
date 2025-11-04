package app.allstackproject.privideo.repository.member.custom;

import static app.allstackproject.privideo.entity.QMemberGroupMapping.memberGroupMapping;
import static app.allstackproject.privideo.entity.QVideoGroupAuthority.videoGroupAuthority;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MemberGroupRepositoryImpl implements MemberGroupRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public boolean isAccessibleToVideo(Long memberId, Long videoId) {
        Long authorityCount = jpaQueryFactory
                .select(videoGroupAuthority.count())
                .from(videoGroupAuthority)
                .where(videoGroupAuthority.video.id.eq(videoId))
                .fetchOne();

        if (authorityCount == null || authorityCount == 0) {
            return true;
        }

        Long accessibleCount = jpaQueryFactory
                .select(videoGroupAuthority.count())
                .from(videoGroupAuthority)
                .join(memberGroupMapping).on(videoGroupAuthority.memberGroup.id.eq(memberGroupMapping.memberGroup.id))
                .where(
                        videoGroupAuthority.video.id.eq(videoId),
                        memberGroupMapping.member.id.eq(memberId)
                )
                .fetchOne();

        return accessibleCount != null && accessibleCount > 0;
    }
}
