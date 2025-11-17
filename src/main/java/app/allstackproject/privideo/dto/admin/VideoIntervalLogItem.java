package app.allstackproject.privideo.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VideoIntervalLogItem {
    private Long segIdx;

    private Long watchCnt;

    private Long quitCnt;

    private Long watchRate;

    private Long quitRate;
}
