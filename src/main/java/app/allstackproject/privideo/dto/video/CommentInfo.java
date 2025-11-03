package app.allstackproject.privideo.dto.video;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CommentInfo {
    private final Long id;

    private final String text;

    private final String creator;

    private final LocalDateTime createdAt;

    @Builder(access = AccessLevel.PRIVATE)
    private CommentInfo(Long id, String text, String creator, LocalDateTime createdAt) {
        this.id = id;
        this.text = text;
        this.creator = creator;
        this.createdAt = createdAt;
    }

    public static CommentInfo of(Long id, String text, String creator, LocalDateTime createdAt) {
        return CommentInfo.builder()
                .id(id)
                .text(text)
                .creator(creator)
                .createdAt(createdAt)
                .build();
    }
}
