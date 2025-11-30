package app.allstackproject.privideo.domain.admin.dto;

import app.allstackproject.privideo.domain.organization.dto.enums.OpenScopeType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminReadAllNoticeItem {
    private Long id;

    private String title;

    private String creator;

    private LocalDateTime createdAt;

    private Long watchCnt;

    private OpenScopeType openScope;

    public AdminReadAllNoticeItem(Long id, String title, String creator, LocalDateTime createdAt, Long watchCnt,
                                  String openScope) {
        this.id = id;
        this.title = title;
        this.creator = creator;
        this.createdAt = createdAt;
        this.watchCnt = watchCnt;
        this.openScope = OpenScopeType.valueOf(openScope);
    }
}
