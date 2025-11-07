package app.allstackproject.privideo.dto.video;

import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class JoinVideoSessionResult {
    private String sessionId;

    private Boolean watchCompleted;

    private VideoInfo video;

    private boolean isComment;

    private final List<String> hashtags;

    private final List<CommentInfo> comments;

    private final List<QuizInfo> quizzes;

    @Builder(access = AccessLevel.PRIVATE)
    private JoinVideoSessionResult(String sessionId, Boolean watchCompleted, VideoInfo video, Boolean isComment,
                                   List<String> hashtags, List<CommentInfo> comments, List<QuizInfo> quizzes) {
        this.sessionId = sessionId;
        this.watchCompleted = watchCompleted;
        this.video = video;
        this.isComment = isComment;
        this.hashtags = hashtags;
        this.comments = comments;
        this.quizzes = quizzes;
    }

    public static JoinVideoSessionResult completed(String sessionId, VideoInfo video, Boolean isComment,
                                                   List<String> hashtags, List<CommentInfo> comments,
                                                   List<QuizInfo> quizzes) {
        return JoinVideoSessionResult.builder()
                .sessionId(sessionId)
                .watchCompleted(true)
                .video(video)
                .isComment(isComment)
                .hashtags(hashtags)
                .comments(comments)
                .quizzes(quizzes)
                .build();
    }

    public static JoinVideoSessionResult create(String sessionId, VideoInfo video, Boolean isComment,
                                                List<String> hashtags, List<CommentInfo> comments,
                                                List<QuizInfo> quizzes) {
        return JoinVideoSessionResult.builder()
                .sessionId(sessionId)
                .watchCompleted(false)
                .video(video)
                .isComment(isComment)
                .hashtags(hashtags)
                .comments(comments)
                .quizzes(quizzes)
                .build();
    }
}
