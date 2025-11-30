package app.allstackproject.privideo.domain.home.dto.response;

import app.allstackproject.privideo.dto.home.HomeVideoItem;
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
