package app.allstackproject.privideo.dto.home;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReadSearchVideoResponse {
    private List<HomeVideoItem> videos;

    private ReadSearchVideoResponse(List<HomeVideoItem> videos) {
        this.videos = videos;
    }

    public static ReadSearchVideoResponse of(List<HomeVideoItem> videos) {
        return new ReadSearchVideoResponse(videos);
    }
}
