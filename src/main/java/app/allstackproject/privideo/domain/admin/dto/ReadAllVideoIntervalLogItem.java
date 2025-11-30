package app.allstackproject.privideo.domain.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReadAllVideoIntervalLogItem {
    private Long id;

    private String title;

    private String creator;

    private Long watchMemberCnt;

    private Long quitRate;
}
