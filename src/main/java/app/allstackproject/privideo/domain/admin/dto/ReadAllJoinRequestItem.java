package app.allstackproject.privideo.domain.admin.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReadAllJoinRequestItem {
    private Long id;

    private String userName;

    private String nickname;

    private LocalDateTime requestedAt;
}
