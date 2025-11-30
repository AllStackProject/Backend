package app.allstackproject.privideo.domain.admin.dto;

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
