package app.allstackproject.privideo.domain.history.dto;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class VideoHistory extends HistoryItem {
    private final Boolean isScrapped;

    public VideoHistory(Long id, String name, String img, Long watchRate, LocalDateTime recentWatch, Long wholeTime,
                        Boolean isScrapped) {
        super(id, name, img, watchRate, recentWatch, wholeTime);
        this.isScrapped = isScrapped;
    }
}
