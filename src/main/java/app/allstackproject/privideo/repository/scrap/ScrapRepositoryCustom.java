package app.allstackproject.privideo.repository.scrap;

public interface ScrapRepositoryCustom {
    boolean isValidMemberAndOrgAndVideo(Long memberId, Long orgId, Long videoId);
}
