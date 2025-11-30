package app.allstackproject.privideo.domain.home.dto.request;

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
