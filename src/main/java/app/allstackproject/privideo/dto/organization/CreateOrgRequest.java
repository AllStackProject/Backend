package app.allstackproject.privideo.dto.organization;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrgRequest {
    @NotBlank(message = "이름은 1글자 이상이어야 합니다.")
    private String name;

    @NotNull
    private String img;

    @NotNull
    private String desc;
}
