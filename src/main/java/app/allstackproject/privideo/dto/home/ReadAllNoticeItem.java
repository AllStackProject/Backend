package app.allstackproject.privideo.dto.home;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReadAllNoticeItem {
    private Long id;

    private String title;

    private LocalDateTime createdAt;

    private Long watchCnt;
}
