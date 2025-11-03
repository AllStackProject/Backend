package app.allstackproject.privideo.repository.member.custom;

public interface MemberGroupRepositoryCustom {
    boolean isAccessibleToVideo(Long memberId, Long videoId);
}
