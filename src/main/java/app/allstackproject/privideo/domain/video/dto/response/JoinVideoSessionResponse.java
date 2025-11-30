package app.allstackproject.privideo.domain.video.dto.response;

import app.allstackproject.privideo.domain.video.enums.AiFunctionType;
import app.allstackproject.privideo.dto.video.JoinVideoSessionResult;
import app.allstackproject.privideo.dto.video.QuizInfo;
import app.allstackproject.privideo.dto.video.VideoInfo;
import java.util.List;
import lombok.Getter;

@Getter
public class JoinVideoSessionResponse {
    private final String sessionId;

    private final String playbackUrl;

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

    private JoinVideoSessionResponse(String sessionId, String playbackUrl, Boolean watchCompleted, VideoInfo video,
                                     List<Long> segViewCnts, Boolean isComment, Boolean isScrapped,
                                     List<String> categories, AiFunctionType aiType, List<QuizInfo> aiQuizzes,
                                     String aiFeedback, String aiSummary) {
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
    }

    public static JoinVideoSessionResponse from(JoinVideoSessionResult result) {
        return new JoinVideoSessionResponse(result.getSessionId(), result.getPlaybackUrl(), result.getWatchCompleted(),
                result.getVideo(), result.getSegViewCnts(), result.getIsComment(), result.getIsScrapped(),
                result.getCategories(), result.getAiType(), result.getAiQuizzes(), result.getAiFeedback(),
                result.getAiSummary());
    }
}
