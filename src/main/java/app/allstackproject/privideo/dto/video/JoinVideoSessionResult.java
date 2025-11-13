package app.allstackproject.privideo.dto.video;

import app.allstackproject.privideo.common.enumStatus.AiResultType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class JoinVideoSessionResult {
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

    @Builder(access = AccessLevel.PRIVATE)
    private JoinVideoSessionResult(String sessionId, Boolean watchCompleted, VideoInfo video, List<Long> segViewCnts,
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

    public static JoinVideoSessionResult completed(String sessionId, VideoInfo video, List<Long> segViewCnts,
                                                   Boolean isComment, Boolean isScrapped, List<String> categories,
                                                   AiResultType aiType, List<QuizInfo> aiQuizzes, String aiFeedback,
                                                   String aiSummary) {
        return JoinVideoSessionResult.builder()
                .sessionId(sessionId)
                .watchCompleted(true)
                .video(video)
                .segViewCnts(segViewCnts)
                .isComment(isComment)
                .isScrapped(isScrapped)
                .categories(categories)
                .aiType(aiType)
                .aiQuizzes(aiQuizzes)
                .aiFeedback(aiFeedback)
                .aiSummary(aiSummary)
                .createdAt(video.getCreatedAt())
                .build();
    }

    public static JoinVideoSessionResult create(String sessionId, VideoInfo video, List<Long> segViewCnts,
                                                Boolean isComment, Boolean isScrapped, List<String> categories,
                                                AiResultType aiType, List<QuizInfo> aiQuizzes, String aiFeedback,
                                                String aiSummary) {
        return JoinVideoSessionResult.builder()
                .sessionId(sessionId)
                .watchCompleted(false)
                .video(video)
                .segViewCnts(segViewCnts)
                .isComment(isComment)
                .isScrapped(isScrapped)
                .categories(categories)
                .aiType(aiType)
                .aiQuizzes(aiQuizzes)
                .aiFeedback(aiFeedback)
                .aiSummary(aiSummary)
                .createdAt(video.getCreatedAt())
                .build();
    }
}
