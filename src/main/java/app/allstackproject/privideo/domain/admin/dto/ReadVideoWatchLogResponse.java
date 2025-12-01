package app.allstackproject.privideo.domain.admin.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReadVideoWatchLogResponse {
    private List<VideoWatchLogItem> watchedMembers;

    private ReadVideoWatchLogResponse(List<VideoWatchLogItem> watchedMembers) {
        this.watchedMembers = watchedMembers;
    }

    public static ReadVideoWatchLogResponse of(List<VideoWatchLogItem> watchedMembers) {
        return new ReadVideoWatchLogResponse(watchedMembers);
    }
}
