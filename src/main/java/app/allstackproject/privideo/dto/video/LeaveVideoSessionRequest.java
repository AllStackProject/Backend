package app.allstackproject.privideo.dto.video;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LeaveVideoSessionRequest {
    @NotBlank
    private String sessionId;

    @NotBlank
    private Long watchRate;

    @NotBlank
    private String watchSegments;

    @NotBlank
    private Long recentPosition;

    @NotBlank
    private Boolean isQuit;
}
