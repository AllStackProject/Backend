package app.allstackproject.privideo.repository.history.custom;

import app.allstackproject.privideo.dto.admin.AllVideoWatchLogItem;
import app.allstackproject.privideo.dto.admin.GroupWatchCompleteRate;
import app.allstackproject.privideo.dto.admin.MemberAvgWatchRateDto;
import app.allstackproject.privideo.dto.admin.MemberWatchLogItem;
import app.allstackproject.privideo.dto.admin.MonthlyWatchItem;
import app.allstackproject.privideo.dto.admin.VideoWatchLogItem;
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
