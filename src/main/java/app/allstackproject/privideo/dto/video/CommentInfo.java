package app.allstackproject.privideo.dto.video;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommentInfo {
    private final Long id;

    private final String text;

    private final String creator;

    private final LocalDateTime createdAt;

    public static CommentInfo of(Long id, String text, String creator, LocalDateTime createdAt) {
        return new CommentInfo(id, text, creator, createdAt);
    }
}
