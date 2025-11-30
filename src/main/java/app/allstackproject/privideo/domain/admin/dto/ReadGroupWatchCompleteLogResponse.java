package app.allstackproject.privideo.domain.admin.dto;

import java.util.List;
import lombok.Getter;

@Getter
public class ReadGroupWatchCompleteLogResponse {
    private Long avgCompleteRate;

    private List<GroupWatchCompleteRate> groupWatchCompleteRates;

    private ReadGroupWatchCompleteLogResponse(Long avgCompleteRate,
                                              List<GroupWatchCompleteRate> groupWatchCompleteRates) {
        this.avgCompleteRate = avgCompleteRate;
        this.groupWatchCompleteRates = groupWatchCompleteRates;
    }

    public static ReadGroupWatchCompleteLogResponse of(List<GroupWatchCompleteRate> groupWatchCompleteRates) {
        Long avg = 0L;
        if (!groupWatchCompleteRates.isEmpty()) {
            avg = (long) groupWatchCompleteRates.stream()
                    .filter(item -> item.getAvgGroupCompleteRate() != null)
                    .mapToLong(GroupWatchCompleteRate::getAvgGroupCompleteRate)
                    .average()
                    .orElse(0.0);
        }

        return new ReadGroupWatchCompleteLogResponse(avg, groupWatchCompleteRates);
    }
}
