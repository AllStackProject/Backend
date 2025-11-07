package app.allstackproject.privideo.dto.quiz;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberQuizDto {
    private Long quizId;

    private Long videoId;
    
    private String videoTitle;

    private String question;

    private String description;

    private Boolean isCorrect;

    private Boolean answer;
}
