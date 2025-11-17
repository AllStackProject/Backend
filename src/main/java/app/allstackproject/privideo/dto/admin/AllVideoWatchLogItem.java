package app.allstackproject.privideo.dto.admin;

import app.allstackproject.privideo.common.enumStatus.VideoOpenScopeType;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class AllVideoWatchLogItem {
    private final Long id;

    private final String title;

    private final String creator;

    private final LocalDateTime expiredAt;

    private final VideoOpenScopeType openScope;

    private final Long watchCompleteRate;

    private final Long watchMemberCnt;

    public AllVideoWatchLogItem(Long id, String title, String creator, LocalDateTime expiredAt, String openScope,
                                Long watchCompleteRate, Long watchMemberCnt) {
        this.id = id;
        this.title = title;
        this.creator = creator;
        this.expiredAt = expiredAt;
        this.openScope = VideoOpenScopeType.valueOf(openScope);
        this.watchCompleteRate = watchCompleteRate;
        this.watchMemberCnt = watchMemberCnt;
    }
}
