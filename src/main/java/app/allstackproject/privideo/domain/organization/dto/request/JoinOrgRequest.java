package app.allstackproject.privideo.domain.organization.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class JoinOrgRequest {
    @NotBlank(message = "조직 코드를 입력해주세요.")
    @Size(min = 6, max = 6, message = "조직 코드는 6자리여야 합니다.")
    private String code;

    @NotBlank(message = "닉네임을 입력해주세요.")
    private String nickname;
}
