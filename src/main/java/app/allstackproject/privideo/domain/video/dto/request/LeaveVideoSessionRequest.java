package app.allstackproject.privideo.domain.video.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LeaveVideoSessionRequest {
    @NotBlank(message = "세션키는 공백일 수 없습니다.")
    private String sessionId;

    @NotNull(message = "시청률은 필수입니다.")
    private Long watchRate;

    @Pattern(regexp = "^[01]+$", message = "watch_segments는 0과 1로만 구성되어야 합니다.")
    @NotBlank(message = "시청 구간별 시청 여부는 필수입니다.")
    private String watchSegments;

    @NotNull(message = "가장 최근 시청 지점은 필수입니다.")
    private Long recentPosition;

    @NotNull(message = "이탈 여부는 필수입니다.")
    private Boolean isQuit;
}
