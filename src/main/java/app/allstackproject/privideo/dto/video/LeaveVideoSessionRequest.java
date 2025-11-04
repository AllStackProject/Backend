package app.allstackproject.privideo.dto.video;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotBlank(message = "시청 구간은 필수입니다.")
    private String watchSegments;

    @NotNull(message = "가장 최근 시청 지점은 필수입니다.")
    private Long recentPosition;

    @NotNull(message = "이탈 여부는 필수입니다.")
    private Boolean isQuit;
}
