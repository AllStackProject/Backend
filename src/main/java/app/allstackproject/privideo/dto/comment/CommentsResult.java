package app.allstackproject.privideo.dto.comment;

import app.allstackproject.privideo.dto.video.CommentInfo;
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

    public static CommentsResult create(List<CommentInfo> comments) {
        List<CommentDto> resultComments = new ArrayList<>();
        List<ChildCommentDto> resultChildComments = new ArrayList<>();

        for (CommentInfo commentInfo : comments) {
            if (commentInfo.getIsChild()) {
                resultChildComments.add(ChildCommentDto.of(commentInfo));
            } else {
                resultComments.add(CommentDto.of(commentInfo));
            }
        }

        return new CommentsResult(resultComments, resultChildComments);
    }
}
