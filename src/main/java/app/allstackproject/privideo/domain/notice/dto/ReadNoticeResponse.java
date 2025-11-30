package app.allstackproject.privideo.domain.notice.dto;

import app.allstackproject.privideo.domain.organization.dto.enums.OpenScopeType;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReadNoticeResponse {
    private String title;

    private String content;

    private LocalDateTime createdAt;

    private Long watchCnt;

    private OpenScopeType openScope;

    private ReadNoticeResponse(String title, String content, LocalDateTime createdAt, Long watchCnt,
                               OpenScopeType openScope) {
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.watchCnt = watchCnt;
        this.openScope = openScope;
    }

    public static ReadNoticeResponse of(String title, String content, LocalDateTime createdAt, Long watchCnt,
                                        OpenScopeType openScope) {
        return new ReadNoticeResponse(title, content, createdAt, watchCnt, openScope);
    }
}
