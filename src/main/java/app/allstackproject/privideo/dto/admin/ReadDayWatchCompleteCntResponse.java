package app.allstackproject.privideo.dto.admin;

import java.util.List;
import lombok.Getter;

@Getter
public class ReadDayWatchCompleteCntResponse {
    private List<Long> dayWatchComplete;

    private ReadDayWatchCompleteCntResponse(List<Long> dayWatchComplete) {
        this.dayWatchComplete = dayWatchComplete;
    }

    public static ReadDayWatchCompleteCntResponse of(List<Long> dayWatchComplete) {
        return new ReadDayWatchCompleteCntResponse(dayWatchComplete);
    }
}
