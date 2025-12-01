package app.allstackproject.privideo.domain.admin.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReadMemberWatchLogResponse {
    private List<MemberWatchLogItem> watchedVideos;

    private ReadMemberWatchLogResponse(List<MemberWatchLogItem> watchedVideos) {
        this.watchedVideos = watchedVideos;
    }

    public static ReadMemberWatchLogResponse of(List<MemberWatchLogItem> watchedVideos) {
        return new ReadMemberWatchLogResponse(watchedVideos);
    }
}
