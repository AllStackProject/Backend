package app.allstackproject.privideo.domain.admin.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReadAllVideoWatchLogResponse {
    private List<AllVideoWatchLogItem> allVideoWatch;

    private ReadAllVideoWatchLogResponse(List<AllVideoWatchLogItem> allVideoWatchLogItems) {
        this.allVideoWatch = allVideoWatchLogItems;
    }

    public static ReadAllVideoWatchLogResponse of(List<AllVideoWatchLogItem> allVideoWatchLogItems) {
        return new ReadAllVideoWatchLogResponse(allVideoWatchLogItems);
    }
}
