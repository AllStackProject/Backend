package app.allstackproject.privideo.dto.admin;

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
