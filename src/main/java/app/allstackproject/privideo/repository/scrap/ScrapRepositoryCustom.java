package app.allstackproject.privideo.repository.scrap;

public interface ScrapRepositoryCustom {
    boolean isValidMemberAndOrgAndVideo(Long memberId, Long orgId, Long videoId);

    int deleteByMemberIdAndVideoId(Long memberId, Long videoId);
}
