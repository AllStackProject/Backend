package app.allstackproject.privideo.dto.admin;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReadQuitLogResponse {
    private List<QuitLogItem> highQuitRateLogs;

    private List<QuitLogItem> lowQuitRateLogs;

    private ReadQuitLogResponse(List<QuitLogItem> highQuitRateLogs, List<QuitLogItem> lowQuitRateLogs) {
        this.highQuitRateLogs = highQuitRateLogs;
        this.lowQuitRateLogs = lowQuitRateLogs;
    }

    public static ReadQuitLogResponse of(List<QuitLogItem> high, List<QuitLogItem> low) {
        return new ReadQuitLogResponse(high, low);
    }
}
