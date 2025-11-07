package app.allstackproject.privideo.dto.video;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QuizInfo {
    private final Long id;

    private final String question;

    // TODO: isCorrect로 수정
    private final Boolean isRight;
}
