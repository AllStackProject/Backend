package app.allstackproject.privideo.domain.member.repository.custom;

import app.allstackproject.privideo.domain.member.entity.MemberGroup;
import java.util.List;

public interface MemberGroupRepositoryCustom {
    boolean isAccessibleToVideo(Long memberId, Long videoId);

    List<MemberGroup> findAllByMemberId(Long memberId);
}
