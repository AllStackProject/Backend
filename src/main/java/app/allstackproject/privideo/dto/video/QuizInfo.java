package app.allstackproject.privideo.dto.video;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QuizInfo {
    private String question;

    private Boolean answer;

    private String description;
}
