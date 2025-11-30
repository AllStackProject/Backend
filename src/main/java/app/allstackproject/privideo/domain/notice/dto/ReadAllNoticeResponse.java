package app.allstackproject.privideo.domain.notice.dto;

import app.allstackproject.privideo.dto.home.ReadAllNoticeItem;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReadAllNoticeResponse {
    private List<ReadAllNoticeItem> notices;

    private ReadAllNoticeResponse(List<ReadAllNoticeItem> notices) {
        this.notices = notices;
    }

    public static ReadAllNoticeResponse of(List<ReadAllNoticeItem> notices) {
        return new ReadAllNoticeResponse(notices);
    }
}
