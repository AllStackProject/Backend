package app.allstackproject.privideo.dto.admin;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReadAllVideosResponse {
    private List<ReadAllVideoDto> vidoes;

    public ReadAllVideosResponse(List<ReadAllVideoDto> vidoes) {
        this.vidoes = vidoes;
    }

    public static ReadAllVideosResponse of(List<ReadAllVideoDto> vidoes) {
        return new ReadAllVideosResponse(vidoes);
    }
}
