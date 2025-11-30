package app.allstackproject.privideo.domain.admin.dto;

import java.util.List;
import lombok.Getter;

@Getter
public class ReadDayWatchCompleteCntResponse {
    private List<Long> dayWatchCnts;

    private ReadDayWatchCompleteCntResponse(List<Long> dayWatchCnts) {
        this.dayWatchCnts = dayWatchCnts;
    }

    public static ReadDayWatchCompleteCntResponse of(List<Long> dayWatchCnts) {
        return new ReadDayWatchCompleteCntResponse(dayWatchCnts);
    }
}
