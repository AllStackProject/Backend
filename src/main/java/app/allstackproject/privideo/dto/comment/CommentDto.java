package app.allstackproject.privideo.dto.comment;

import app.allstackproject.privideo.entity.Comment;

public class CommentDto {
    private final Long id;

    private final String text;

    protected CommentDto(Long id, String text) {
        this.id = id;
        this.text = text;
    }

    public static CommentDto of(Comment comment) {
        return new CommentDto(comment.getId(), comment.getText());
    }
}
