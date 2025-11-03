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
        Integer count = jpaQueryFactory
                .selectOne()
                .from(videoGroupAuthority)
                .join(memberGroupMapping).on(videoGroupAuthority.memberGroup.id.eq(memberGroupMapping.member.id))
                .where(videoGroupAuthority.video.id.eq(videoId))
                .fetchFirst();

        return count != null;
    }
}
