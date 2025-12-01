package app.allstackproject.privideo.domain.user.dto.request;

import app.allstackproject.privideo.global.annotation.PasswordConstraint;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostLoginRequest {
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @PasswordConstraint
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
}
