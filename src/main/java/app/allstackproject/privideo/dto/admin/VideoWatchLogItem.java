package app.allstackproject.privideo.dto.admin;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VideoWatchLogItem {
    private final String nickname;

    private final List<String> groups;

    private final Long watchRate;

    private final LocalDateTime watchedAt;
}
