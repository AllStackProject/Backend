package app.allstackproject.privideo.dto.admin;

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
            for (GroupWatchCompleteRate item : groupWatchCompleteRates) {
                avg += item.getAvgGroupCompleteRate();
            }
            avg /= groupWatchCompleteRates.size();
        }

        return new ReadGroupWatchCompleteLogResponse(avg, groupWatchCompleteRates);
    }
}
