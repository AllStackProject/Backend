package app.allstackproject.privideo.dto.video;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class QuizInfo {
    private final Long id;

    private final String question;

    private final Boolean isRight;

    @Builder(access = AccessLevel.PRIVATE)
    private QuizInfo(Long id, String question, Boolean isRight) {
        this.id = id;
        this.question = question;
        this.isRight = isRight;
    }

    public static QuizInfo of(Long id, String question, Boolean isRight) {
        return QuizInfo.builder()
                .id(id)
                .question(question)
                .isRight(isRight)
                .build();
    }
}
