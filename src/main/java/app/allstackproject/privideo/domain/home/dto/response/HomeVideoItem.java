package app.allstackproject.privideo.domain.home.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class HomeVideoItem {
    private Long id;

    private String title;

    @Setter
    private String thumbnailUrl;

    private String creator;

    private Long wholeTime;

    private Long watchCnt;

    private LocalDateTime createdAt;

    private Boolean isScrapped;

    private List<String> categories;

    public HomeVideoItem(Long id, String title, String thumbnailUrl, String creator, Long wholeTime, Long watchCnt,
                         LocalDateTime createdAt, Boolean isScrapped) {
        this.id = id;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.creator = creator;
        this.wholeTime = wholeTime;
        this.watchCnt = watchCnt;
        this.createdAt = createdAt;
        this.isScrapped = isScrapped;
    }
}
