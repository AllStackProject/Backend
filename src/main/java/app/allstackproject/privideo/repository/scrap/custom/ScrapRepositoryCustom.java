package app.allstackproject.privideo.repository.scrap.custom;

import app.allstackproject.privideo.dto.history.HistoryItem;
import java.util.List;

public interface ScrapRepositoryCustom {
    boolean isValidMemberAndOrgAndVideo(Long memberId, Long orgId, Long videoId);

    int deleteByMemberIdAndVideoId(Long memberId, Long videoId);

    List<HistoryItem> findByMemberIdAndOrganizationId(Long memberId, Long orgId);
}
