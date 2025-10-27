package app.allstackproject.privideo.dto;

import app.allstackproject.privideo.entity.MemberQuizResult;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserQuizResponse {

    private List<QuizItem> allQuiz;

    public static UserQuizResponse of(List<MemberQuizResult> quizList) {

        List<QuizItem> allQuiz = quizList.stream()
                .collect(Collectors.groupingBy(q -> q.getVideo().getTitle()))
                .entrySet().stream()
                .map(entry -> QuizItem.builder()
                        .videoName(entry.getKey())
                        .quiz(entry.getValue().stream()
                                .map(q -> QuizDetail.builder()
                                        .id(q.getId())
                                        .question(q.getQuiz().getQuestion())
                                        .description(q.getQuiz().getDescription())
                                        .isCorrect(q.isCorrect())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());

        return UserQuizResponse.builder()
                .allQuiz(allQuiz)
                .build();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuizItem {
        private String videoName;
        private List<QuizDetail> quiz;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuizDetail {
        private Long id;
        private String question;
        private String description;
        private Boolean isCorrect;
    }
}
