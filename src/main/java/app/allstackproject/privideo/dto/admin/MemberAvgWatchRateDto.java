package app.allstackproject.privideo.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberAvgWatchRateDto {
    private Long memberId;

    private Long avgWatchRate;
}
