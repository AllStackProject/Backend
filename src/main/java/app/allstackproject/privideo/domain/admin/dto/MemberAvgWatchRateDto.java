package app.allstackproject.privideo.domain.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberAvgWatchRateDto {
    private Long memberId;

    private Long avgWatchRate;
}
