package app.allstackproject.privideo.domain.admin.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReadAllMemberWatchLogResponse {
    private List<AllMemberWatchLogItem> allMemberWatch;

    private ReadAllMemberWatchLogResponse(List<AllMemberWatchLogItem> allMemberWatchLogItems) {
        this.allMemberWatch = allMemberWatchLogItems;
    }

    public static ReadAllMemberWatchLogResponse of(List<AllMemberWatchLogItem> allMemberWatchLogItems) {
        return new ReadAllMemberWatchLogResponse(allMemberWatchLogItems);
    }
}
