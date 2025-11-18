package app.allstackproject.privideo.dto.home;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HomeVideoItem {
    private Long id;

    private String title;

    private String thumbnailUrl;

    private String creator;

    private Long watchCnt;

    private LocalDateTime createdAt;

    private Boolean isScrapped;

    private List<String> categories;
}
