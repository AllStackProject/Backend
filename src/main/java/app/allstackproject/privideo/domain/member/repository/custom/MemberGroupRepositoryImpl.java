package app.allstackproject.privideo.domain.member.repository.custom;

import static app.allstackproject.privideo.domain.member.entity.QMemberGroup.memberGroup;
import static app.allstackproject.privideo.domain.member.entity.QMemberGroupMapping.memberGroupMapping;
import static app.allstackproject.privideo.domain.organization.dto.enums.JoinStatusType.APPROVED;
import static app.allstackproject.privideo.domain.video.entity.QVideoMemberGroupMapping.videoMemberGroupMapping;
import static app.allstackproject.privideo.domain.video.enums.UploadStatusType.COMPLETE;
import static app.allstackproject.privideo.shared.enums.BaseStatusType.ACTIVE;

import app.allstackproject.privideo.domain.member.entity.MemberGroup;
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
