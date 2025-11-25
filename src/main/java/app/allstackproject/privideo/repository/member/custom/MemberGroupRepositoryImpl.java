package app.allstackproject.privideo.repository.member.custom;

import static app.allstackproject.privideo.common.enumStatus.BaseStatusType.ACTIVE;
import static app.allstackproject.privideo.common.enumStatus.JoinStatusType.APPROVED;
import static app.allstackproject.privideo.common.enumStatus.UploadStatusType.COMPLETE;
import static app.allstackproject.privideo.entity.QMemberGroup.memberGroup;
import static app.allstackproject.privideo.entity.QMemberGroupMapping.memberGroupMapping;
import static app.allstackproject.privideo.entity.QVideo.video;
import static app.allstackproject.privideo.entity.QVideoMemberGroupMapping.videoMemberGroupMapping;

import app.allstackproject.privideo.entity.MemberGroup;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MemberGroupRepositoryImpl implements MemberGroupRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public boolean isAccessibleToVideo(Long memberId, Long videoId) {
        Long authorityCount = jpaQueryFactory
                .select(videoMemberGroupMapping.count())
                .from(videoMemberGroupMapping)
                .where(
                        videoMemberGroupMapping.video.id.eq(videoId),
                        videoMemberGroupMapping.video.uploadStatus.eq(COMPLETE)
                )
                .fetchOne();

        if (authorityCount == null || authorityCount == 0) {
            return true;
        }

        Long accessibleCount = jpaQueryFactory
                .select(videoMemberGroupMapping.count())
                .from(videoMemberGroupMapping)
                .join(memberGroupMapping)
                .on(videoMemberGroupMapping.memberGroup.id.eq(memberGroupMapping.memberGroup.id))
                .where(
                        videoMemberGroupMapping.video.id.eq(videoId),
                        videoMemberGroupMapping.video.uploadStatus.eq(COMPLETE)
                )
                .fetchOne();

        return accessibleCount != null && accessibleCount > 0;
    }

    @Override
    public List<MemberGroup> findAllByMemberId(Long memberId) {
        return jpaQueryFactory
                .select(memberGroup)
                .from(memberGroupMapping)
                .join(memberGroupMapping.memberGroup, memberGroup)
                .where(
                        memberGroupMapping.member.id.eq(memberId),
                        memberGroupMapping.member.joinStatus.eq(APPROVED),
                        memberGroupMapping.member.status.eq(ACTIVE)
                )
                .fetch();
    }
}
