package app.allstackproject.privideo.dto.history;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HistoryItem {
    private Long id;

    private String name;

    private String img;

    private Long watchRate;

    private LocalDateTime recentWatch;

    private Long wholeTime;
}
