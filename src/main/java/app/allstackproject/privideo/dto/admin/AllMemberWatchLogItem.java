package app.allstackproject.privideo.dto.admin;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class AllMemberWatchLogItem {
    private Long id;

    private String nickname;

    private List<String> groups;

    @Setter
    private Long avgWatchRate;
}
