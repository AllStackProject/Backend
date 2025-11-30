package app.allstackproject.privideo.domain.video.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReadVideoEncodingResultRequest {
    @NotNull
    private String videoUuid;

    @NotNull
    private String status;

    @NotNull
    private String message;
}
