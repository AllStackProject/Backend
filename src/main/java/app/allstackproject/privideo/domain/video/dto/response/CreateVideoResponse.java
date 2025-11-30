package app.allstackproject.privideo.domain.video.dto.response;

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
