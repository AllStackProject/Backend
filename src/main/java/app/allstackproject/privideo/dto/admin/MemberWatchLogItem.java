package app.allstackproject.privideo.dto.admin;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberWatchLogItem {
    private Long id;

    private String title;

    private Long watchRate;

    private LocalDateTime watchedAt;
}
