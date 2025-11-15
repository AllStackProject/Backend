package app.allstackproject.privideo.dto.admin;

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
