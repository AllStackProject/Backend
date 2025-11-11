package app.allstackproject.privideo.dto.history;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class VideoHistory extends HistoryItem {
    private final Boolean isScrapped;

    public VideoHistory(Long id, String name, String img, Long watchRate, LocalDateTime recentWatch,
                        Boolean isScrapped) {
        super(id, name, img, watchRate, recentWatch);
        this.isScrapped = isScrapped;
    }
}
