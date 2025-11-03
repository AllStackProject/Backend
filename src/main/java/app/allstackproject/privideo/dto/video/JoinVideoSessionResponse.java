package app.allstackproject.privideo.dto.video;

import java.util.List;
import lombok.Getter;

@Getter
public class JoinVideoSessionResponse {
    private final String sessionId;

    private final Boolean watchCompleted;

    private final VideoInfo video;

    private final Boolean isComment;

    private final List<String> hashtags;

    private final List<CommentInfo> comments;

    private final List<QuizInfo> quizzes;


    private JoinVideoSessionResponse(String sessionId, Boolean watchCompleted, VideoInfo video, Boolean isComment,
                                     List<String> hashtags, List<CommentInfo> comments, List<QuizInfo> quizzes) {
        this.sessionId = sessionId;
        this.watchCompleted = watchCompleted;
        this.video = video;
        this.isComment = isComment;
        this.hashtags = hashtags == null ? List.of() : List.copyOf(hashtags);
        this.comments = comments == null ? List.of() : List.copyOf(comments);
        this.quizzes = quizzes == null ? List.of() : List.copyOf(quizzes);
    }

    public static JoinVideoSessionResponse from(JoinVideoSessionResult result) {
        return new JoinVideoSessionResponse(result.getSessionId(), result.getWatchCompleted(), result.getVideo(),
                result.isComment(), result.getHashtags(), result.getComments(), result.getQuizzes());
    }
}
