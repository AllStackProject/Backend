package app.allstackproject.privideo.dto.user;

import app.allstackproject.privideo.common.annotation.AgeTypeConstraint;
import app.allstackproject.privideo.common.annotation.PasswordConstraint;
import app.allstackproject.privideo.common.enumStatus.GenderType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostSignupRequest {

    @NotBlank(message = "이름은 1글자 이상이어야 합니다.")
    private String name;

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @PasswordConstraint
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @NotNull(message = "성별을 선택해 주세요.")
    private GenderType gender;

    @AgeTypeConstraint
    private Integer age;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String phoneNumber;

    private String organizationCode;
}
