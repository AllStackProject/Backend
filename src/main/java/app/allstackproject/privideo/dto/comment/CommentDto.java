package app.allstackproject.privideo.dto.comment;

import app.allstackproject.privideo.dto.video.CommentInfo;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class CommentDto {
    private final Long id;

    private final String text;

    private final String creator;

    private final LocalDateTime createdAt;

    protected CommentDto(Long id, String text, String creator, LocalDateTime createdAt) {
        this.id = id;
        this.text = text;
        this.creator = creator;
        this.createdAt = createdAt;
    }

    public static CommentDto of(CommentInfo commentInfo) {
        return new CommentDto(commentInfo.getId(), commentInfo.getText(), commentInfo.getCreator(),
                commentInfo.getCreatedAt());
    }
}
