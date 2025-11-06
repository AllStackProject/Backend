package app.allstackproject.privideo.dto.comment;

import java.util.List;
import lombok.Getter;

@Getter
public class ReadCommentsResponse {

    private final List<CommentDto> comments;

    private final List<ChildCommentDto> childComments;

    private ReadCommentsResponse(List<CommentDto> comments, List<ChildCommentDto> childComments) {
        this.comments = comments;
        this.childComments = childComments;
    }

    public static ReadCommentsResponse of(CommentsResult commentsResult) {
        return new ReadCommentsResponse(commentsResult.getComments(), commentsResult.getChildComments());
    }
}
