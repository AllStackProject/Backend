package app.allstackproject.privideo.dto.admin;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminReadAllNoticeResponse {
    private List<AdminReadAllNoticeItem> notices;

    private AdminReadAllNoticeResponse(List<AdminReadAllNoticeItem> notices) {
        this.notices = notices;
    }

    public static AdminReadAllNoticeResponse of(List<AdminReadAllNoticeItem> notices) {
        return new AdminReadAllNoticeResponse(notices);
    }
}
