package app.allstackproject.privideo.dto.home;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ModifyNicknameRequest {
    @NotBlank
    private String nickname;
}
