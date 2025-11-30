package app.allstackproject.privideo.domain.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GroupWatchCompleteRate {
    private String name;

    private Long avgGroupCompleteRate;
}
