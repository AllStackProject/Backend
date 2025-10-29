package app.allstackproject.privideo.dto.organization;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class JoinOrgRequest {
    @NotBlank(message = "이름은 1글자 이상이어야 합니다.")
    private String name;

    @NotBlank(message = "조직 코드를 입력해주세요.")
    @Size(min = 6, max = 6, message = "조직 코드는 6자리여야 합니다.")
    private String code;
}
