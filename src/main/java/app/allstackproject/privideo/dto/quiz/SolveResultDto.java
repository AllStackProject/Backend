package app.allstackproject.privideo.dto.quiz;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SolveResultDto {
    @NotNull
    private Long quizId;

    @NotNull
    private Boolean memberAnswer;
}
