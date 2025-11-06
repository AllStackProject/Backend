package app.allstackproject.privideo.dto.comment;

import app.allstackproject.privideo.entity.Comment;

public class ChildCommentDto extends CommentDto {
    private final Long parentCommentId;

    private ChildCommentDto(Long id, String text, Long parentCommentId) {
        super(id, text);
        this.parentCommentId = parentCommentId;
    }

    public static ChildCommentDto of(Comment comment) {
        return new ChildCommentDto(comment.getId(), comment.getText(), comment.getParentCommentId());
    }
}
