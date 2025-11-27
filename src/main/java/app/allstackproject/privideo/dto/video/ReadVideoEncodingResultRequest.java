package app.allstackproject.privideo.dto.video;

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
