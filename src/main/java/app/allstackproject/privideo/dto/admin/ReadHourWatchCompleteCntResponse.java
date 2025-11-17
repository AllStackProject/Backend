package app.allstackproject.privideo.dto.admin;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReadHourWatchCompleteCntResponse {
    private List<Long> hourWatchCnts;

    private ReadHourWatchCompleteCntResponse(List<Long> hourWatchCnts) {
        this.hourWatchCnts = hourWatchCnts;
    }

    public static ReadHourWatchCompleteCntResponse of(List<Long> hourWatchCnts) {
        return new ReadHourWatchCompleteCntResponse(hourWatchCnts);
    }
}
