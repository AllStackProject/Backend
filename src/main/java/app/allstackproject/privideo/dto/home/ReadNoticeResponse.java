package app.allstackproject.privideo.dto.home;

import app.allstackproject.privideo.common.enumStatus.OpenScopeType;
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
