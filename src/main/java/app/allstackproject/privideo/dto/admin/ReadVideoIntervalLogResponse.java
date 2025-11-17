package app.allstackproject.privideo.dto.admin;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReadVideoIntervalLogResponse {
    private List<VideoIntervalLogItem> videoIntervalLogItems;

    private ReadVideoIntervalLogResponse(List<VideoIntervalLogItem> videoIntervalLogItems) {
        this.videoIntervalLogItems = videoIntervalLogItems;
    }

    public static ReadVideoIntervalLogResponse of(List<VideoIntervalLogItem> videoIntervalLogItems) {
        return new ReadVideoIntervalLogResponse(videoIntervalLogItems);
    }
}
