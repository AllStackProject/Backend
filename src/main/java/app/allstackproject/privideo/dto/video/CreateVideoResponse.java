package app.allstackproject.privideo.dto.video;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateVideoResponse {
    private String presignedUrl;

    private Long videoId;

    private CreateVideoResponse(String presignedUrl, Long videoId) {
        this.presignedUrl = presignedUrl;
        this.videoId = videoId;
    }

    public static CreateVideoResponse of(String presignedUrl, Long videoId) {
        return new CreateVideoResponse(presignedUrl, videoId);
    }
}
