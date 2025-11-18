package app.allstackproject.privideo.repository.member.custom;

import java.util.List;

public interface MemberGroupRepositoryCustom {
    boolean isAccessibleToVideo(Long memberId, Long videoId);

    List<String> findAllByMemberId(Long memberId);
}
