package app.allstackproject.privideo.dto.quiz;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResponse {

    private List<QuizItem> allQuiz;

    public static QuizResponse of(List<MemberQuizDto> rows) {
        Map<Long, List<MemberQuizDto>> grouped = new LinkedHashMap<>();
        for (MemberQuizDto d : rows) {
            grouped.computeIfAbsent(d.getVideoId(), k -> new ArrayList<>()).add(d);
        }

        List<QuizItem> items = new ArrayList<>(grouped.size());
        for (Map.Entry<Long, List<MemberQuizDto>> e : grouped.entrySet()) {
            Long videoId = e.getKey();
            List<MemberQuizDto> list = e.getValue();
            String videoTitle = list.isEmpty() ? null : list.get(0).getVideoTitle();

            List<QuizDetail> details = new ArrayList<>(list.size());
            for (MemberQuizDto d : list) {
                details.add(QuizDetail.builder()
                        .id(d.getQuizId())
                        .question(d.getQuestion())
                        .description(d.getDescription())
                        .isCorrect(d.getIsCorrect())
                        .answer(d.getAnswer())
                        .build());
            }

            items.add(QuizItem.builder()
                    .videoId(videoId)
                    .videoName(videoTitle)
                    .quiz(details)
                    .build());
        }

        return QuizResponse.builder().allQuiz(items).build();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuizItem {
        private Long videoId;
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
        private Boolean answer;
    }
}
