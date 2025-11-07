package app.allstackproject.privideo.dto.comment;

import app.allstackproject.privideo.entity.Comment;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class CommentsResult {
    private final List<CommentDto> comments;

    private final List<ChildCommentDto> childComments;

    private CommentsResult(List<CommentDto> comments, List<ChildCommentDto> childComments) {
        this.comments = comments;
        this.childComments = childComments;
    }

    public static CommentsResult create(List<Comment> comments) {
        List<CommentDto> resultComments = new ArrayList<>();
        List<ChildCommentDto> resultChildComments = new ArrayList<>();

        for (Comment comment : comments) {
            if (comment.isChild()) {
                resultChildComments.add(ChildCommentDto.of(comment));
            } else {
                resultComments.add(CommentDto.of(comment));
            }
        }

        return new CommentsResult(resultComments, resultChildComments);
    }
}
