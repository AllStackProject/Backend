package app.allstackproject.privideo.dto.video;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;

@Getter
public class JoinVideoSessionResponse {
    private final String sessionId;

    private final Boolean watchCompleted;

    private final VideoInfo video;

    private final List<Long> segViewCnts;

    private final Boolean isComment;

    private final Boolean isScrapped;

    private final List<String> categories;

    private final List<QuizInfo> quizzes;

    private final LocalDateTime createdAt;

    private JoinVideoSessionResponse(String sessionId, Boolean watchCompleted, VideoInfo video, List<Long> segViewCnts,
                                     Boolean isComment, Boolean isScrapped, List<String> categories,
                                     List<QuizInfo> quizzes, LocalDateTime createdAt) {
        this.sessionId = sessionId;
        this.watchCompleted = watchCompleted;
        this.video = video;
        this.segViewCnts = segViewCnts == null ? List.of() : List.copyOf(segViewCnts);
        this.isComment = isComment;
        this.isScrapped = isScrapped;
        this.categories = categories == null ? List.of() : List.copyOf(categories);
        this.quizzes = quizzes == null ? List.of() : List.copyOf(quizzes);
        this.createdAt = createdAt;
    }

    public static JoinVideoSessionResponse from(JoinVideoSessionResult result) {
        return new JoinVideoSessionResponse(result.getSessionId(), result.getWatchCompleted(), result.getVideo(),
                result.getSegViewCnts(), result.getIsComment(), result.getIsScrapped(), result.getCategories(),
                result.getQuizzes(), result.getCreatedAt());
    }
}
