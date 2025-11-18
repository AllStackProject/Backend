package app.allstackproject.privideo.dto.admin;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VideoRankItem {
    private String title;

    private LocalDateTime createdAt;

    private Long watchCnt;

    private Long watchCompleteRate;
}
