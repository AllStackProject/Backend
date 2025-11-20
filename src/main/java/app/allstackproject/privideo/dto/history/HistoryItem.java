package app.allstackproject.privideo.dto.history;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class HistoryItem {
    private Long id;

    private String name;

    @Setter
    private String img;

    private Long watchRate;

    private LocalDateTime recentWatch;

    private Long wholeTime;
}
