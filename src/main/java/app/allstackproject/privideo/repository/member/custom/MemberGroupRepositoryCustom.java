package app.allstackproject.privideo.repository.member.custom;

import app.allstackproject.privideo.entity.MemberGroup;
import java.util.List;

public interface MemberGroupRepositoryCustom {
    boolean isAccessibleToVideo(Long memberId, Long videoId);

    List<MemberGroup> findAllByMemberId(Long memberId);
}
