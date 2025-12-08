package app.allstackproject.privideo.domain.video.dto.response;

import app.allstackproject.privideo.domain.video.enums.AiFunctionType;
import app.allstackproject.privideo.domain.quiz.dto.QuizInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
public class JoinVideoSessionResponse {
    private final String sessionId;

    @Setter
    private String playbackUrl;

    private final Boolean watchCompleted;

    private final VideoInfo video;

    private final List<Long> segViewCnts;

    private final Boolean isComment;

    private final Boolean isScrapped;

    private final List<String> categories;

    private final AiFunctionType aiType;

    private final List<QuizInfo> aiQuizzes;

    private final String aiFeedback;

    private final String aiSummary;

    @Setter
    @JsonIgnore
    private Boolean fallbackToS3;

    private JoinVideoSessionResponse(String sessionId, String playbackUrl, Boolean watchCompleted, VideoInfo video,
                                     List<Long> segViewCnts, Boolean isComment, Boolean isScrapped,
                                     List<String> categories, AiFunctionType aiType, List<QuizInfo> aiQuizzes,
                                     String aiFeedback, String aiSummary, Boolean fallbackToS3) {
        this.sessionId = sessionId;
        this.playbackUrl = playbackUrl;
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
        this.fallbackToS3 = fallbackToS3;
    }

    public static JoinVideoSessionResponse from(JoinVideoSessionResult result) {
        return new JoinVideoSessionResponse(result.getSessionId(), result.getPlaybackUrl(), result.getWatchCompleted(),
                result.getVideo(), result.getSegViewCnts(), result.getIsComment(), result.getIsScrapped(),
                result.getCategories(), result.getAiType(), result.getAiQuizzes(), result.getAiFeedback(),
                result.getAiSummary(), false);
    }
}
