package app.allstackproject.privideo.domain.history.repository.custom;

import app.allstackproject.privideo.domain.admin.dto.AllVideoWatchLogItem;
import app.allstackproject.privideo.domain.admin.dto.GroupWatchCompleteRate;
import app.allstackproject.privideo.domain.admin.dto.MemberAvgWatchRateDto;
import app.allstackproject.privideo.domain.admin.dto.MemberWatchLogItem;
import app.allstackproject.privideo.domain.admin.dto.MonthlyWatchItem;
import app.allstackproject.privideo.domain.admin.dto.VideoWatchLogItem;
import app.allstackproject.privideo.dto.history.VideoHistory;
import java.time.LocalDateTime;
import java.util.List;

public interface HistoryRepositoryCustom {
    List<VideoHistory> findByMemberId(Long memberId);

    List<MemberWatchLogItem> findWatchLogByMemberId(Long memberId);

    List<MemberAvgWatchRateDto> findMemberAvgWatchRateByOrgId(Long orgId);

    List<AllVideoWatchLogItem> findAllVideoWatchLogByOrgId(Long orgId);

    List<VideoWatchLogItem> findVideoWatchLogByVideoId(Long videoId);

    List<String> findTopCategoriesByMemberIdWithinPeriod(Long memberId, LocalDateTime startDate, LocalDateTime endDate);

    List<MonthlyWatchItem> findMonthlyStatsByMemberIdWithinPeriod(Long memberId, LocalDateTime startDate,
                                                                  LocalDateTime endDate);

    List<GroupWatchCompleteRate> findGroupAvgWatchRateByOrgIdWithinPeriod(Long orgId, LocalDateTime startDate,
                                                                          LocalDateTime endDate);
}
