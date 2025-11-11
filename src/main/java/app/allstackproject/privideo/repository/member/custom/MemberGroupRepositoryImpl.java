package app.allstackproject.privideo.repository.member.custom;

import static app.allstackproject.privideo.common.enumStatus.BaseStatusType.ACTIVE;
import static app.allstackproject.privideo.entity.QMemberGroupMapping.memberGroupMapping;
import static app.allstackproject.privideo.entity.QVideoMemberGroupMapping.videoMemberGroupMapping;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MemberGroupRepositoryImpl implements MemberGroupRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public boolean isAccessibleToVideo(Long memberId, Long videoId) {
        Long authorityCount = jpaQueryFactory
                .select(videoMemberGroupMapping.count())
                .from(videoMemberGroupMapping)
                .where(videoMemberGroupMapping.video.id.eq(videoId)
                        .and(videoMemberGroupMapping.memberGroup.status.eq(ACTIVE))
                        .and(videoMemberGroupMapping.video.status.eq(ACTIVE)))
                .fetchOne();

        if (authorityCount == null || authorityCount == 0) {
            return true;
        }

        Long accessibleCount = jpaQueryFactory
                .select(videoMemberGroupMapping.count())
                .from(videoMemberGroupMapping)
                .join(memberGroupMapping)
                .on(videoMemberGroupMapping.memberGroup.id.eq(memberGroupMapping.memberGroup.id))
                .where(videoMemberGroupMapping.video.id.eq(videoId)
                        .and(videoMemberGroupMapping.memberGroup.status.eq(ACTIVE))
                        .and(videoMemberGroupMapping.video.status.eq(ACTIVE)))
                .fetchOne();

        return accessibleCount != null && accessibleCount > 0;
    }
}
