package app.allstackproject.privideo.dto.admin;

import app.allstackproject.privideo.common.enumStatus.OpenScopeType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReadAllNoticeItem {
    private Long id;

    private String title;

    private String creator;

    private LocalDateTime createdAt;

    private Long watchCnt;

    private OpenScopeType openScope;

    public ReadAllNoticeItem(Long id, String title, String creator, LocalDateTime createdAt, Long watchCnt,
                             String openScope) {
        this.id = id;
        this.title = title;
        this.creator = creator;
        this.createdAt = createdAt;
        this.watchCnt = watchCnt;
        this.openScope = OpenScopeType.valueOf(openScope);
    }
}
