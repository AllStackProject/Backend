package app.allstackproject.privideo.domain.comment.dto.response;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class ChildCommentDto extends CommentDto {
    private final Long parentCommentId;

    private ChildCommentDto(Long id, String text, Long parentCommentId, String creator, LocalDateTime createdAt) {
        super(id, text, creator, createdAt);
        this.parentCommentId = parentCommentId;
    }

    public static ChildCommentDto of(CommentInfo commentInfo) {
        return new ChildCommentDto(commentInfo.getId(), commentInfo.getText(), commentInfo.getParentCommentId(),
                commentInfo.getCreator(), commentInfo.getCreatedAt());
    }
}
