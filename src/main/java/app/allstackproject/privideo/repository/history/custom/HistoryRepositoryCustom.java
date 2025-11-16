package app.allstackproject.privideo.repository.history.custom;

import app.allstackproject.privideo.dto.admin.AllVideoWatchLogItem;
import app.allstackproject.privideo.dto.admin.MemberAvgWatchRateDto;
import app.allstackproject.privideo.dto.admin.MemberWatchLogItem;
import app.allstackproject.privideo.dto.history.VideoHistory;
import java.util.List;

public interface HistoryRepositoryCustom {
    List<VideoHistory> findByMemberId(Long memberId);

    List<MemberWatchLogItem> findWatchLogByMemberId(Long memberId);

    List<MemberAvgWatchRateDto> findAvgWatchRateByOrgId(Long orgId);

    List<AllVideoWatchLogItem> findAllVideoWatchLogByOrgId(Long orgId);
}
