package app.allstackproject.privideo.dto.admin;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReadAllVideosResponse {
    private List<ReadAllVideoItem> vidoes;

    private ReadAllVideosResponse(List<ReadAllVideoItem> vidoes) {
        this.vidoes = vidoes;
    }

    public static ReadAllVideosResponse of(List<ReadAllVideoItem> vidoes) {
        return new ReadAllVideosResponse(vidoes);
    }
}
