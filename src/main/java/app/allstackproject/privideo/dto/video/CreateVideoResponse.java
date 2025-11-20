package app.allstackproject.privideo.dto.video;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateVideoResponse {
    private String presignedUrl;

    private CreateVideoResponse(String presignedUrl) {
        this.presignedUrl = presignedUrl;
    }

    public static CreateVideoResponse of(String presignedUrl) {
        return new CreateVideoResponse(presignedUrl);
    }
}
