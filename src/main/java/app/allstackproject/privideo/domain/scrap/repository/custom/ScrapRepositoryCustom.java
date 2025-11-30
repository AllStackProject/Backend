package app.allstackproject.privideo.domain.scrap.repository.custom;

import app.allstackproject.privideo.domain.history.dto.HistoryItem;
import java.util.List;

public interface ScrapRepositoryCustom {
    boolean isValidMemberAndOrgAndVideo(Long memberId, Long orgId, Long videoId);

    int deleteByMemberIdAndVideoId(Long memberId, Long videoId);

    List<HistoryItem> findByMemberIdAndOrganizationId(Long memberId, Long orgId);
}
