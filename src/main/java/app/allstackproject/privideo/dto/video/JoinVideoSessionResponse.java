package app.allstackproject.privideo.dto.video;

import app.allstackproject.privideo.common.enumStatus.AiResultType;
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

    private final AiResultType aiType;

    private final List<QuizInfo> aiQuizzes;

    private final String aiFeedback;

    private final String aiSummary;

    private final LocalDateTime createdAt;

    private JoinVideoSessionResponse(String sessionId, Boolean watchCompleted, VideoInfo video, List<Long> segViewCnts,
                                     Boolean isComment, Boolean isScrapped, List<String> categories,
                                     AiResultType aiType, List<QuizInfo> aiQuizzes, String aiFeedback, String aiSummary,
                                     LocalDateTime createdAt) {
        this.sessionId = sessionId;
        this.watchCompleted = watchCompleted;
        this.video = video;
        this.segViewCnts = segViewCnts == null ? List.of() : List.copyOf(segViewCnts);
        this.isComment = isComment;
        this.isScrapped = isScrapped;
        this.categories = categories == null ? List.of() : List.copyOf(categories);
        this.aiType = aiType;
        this.aiQuizzes = aiQuizzes == null ? List.of() : List.copyOf(aiQuizzes);
        this.aiFeedback = aiFeedback;
        this.aiSummary = aiSummary;
        this.createdAt = createdAt;
    }

    public static JoinVideoSessionResponse from(JoinVideoSessionResult result) {
        return new JoinVideoSessionResponse(result.getSessionId(), result.getWatchCompleted(), result.getVideo(),
                result.getSegViewCnts(), result.getIsComment(), result.getIsScrapped(), result.getCategories(),
                result.getAiType(), result.getAiQuizzes(), result.getAiFeedback(), result.getAiSummary(),
                result.getCreatedAt());
    }
}
