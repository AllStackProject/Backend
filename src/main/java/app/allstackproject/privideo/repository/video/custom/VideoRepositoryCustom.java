package app.allstackproject.privideo.repository.video.custom;

public interface VideoRepositoryCustom {
    boolean isValidMemberAndOrgAndVideo(Long memberId, Long orgId, Long videoId);
}
