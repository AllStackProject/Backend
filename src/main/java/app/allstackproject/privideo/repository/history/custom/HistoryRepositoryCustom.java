package app.allstackproject.privideo.repository.history.custom;

import app.allstackproject.privideo.dto.history.VideoHistory;
import java.util.List;

public interface HistoryRepositoryCustom {
    List<VideoHistory> findByMemberIdAndOrganizationId(Long memberId, Long orgId);
}
