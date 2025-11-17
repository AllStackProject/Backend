package app.allstackproject.privideo.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GroupWatchCompleteRate {
    private String name;

    private Long avgGroupCompleteRate;
}
