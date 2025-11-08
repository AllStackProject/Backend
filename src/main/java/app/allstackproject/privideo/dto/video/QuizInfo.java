package app.allstackproject.privideo.dto.video;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QuizInfo {
    private final Long id;

    private final String question;

    private final Boolean answer;

    private final Boolean memberAnswer;
    
    private final String description;
}
