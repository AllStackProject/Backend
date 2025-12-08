package app.allstackproject.privideo.domain.user.dto.request;

import app.allstackproject.privideo.global.annotation.AgeTypeConstraint;
import app.allstackproject.privideo.global.annotation.EnumConstraint;
import app.allstackproject.privideo.global.annotation.PasswordConstraint;
import app.allstackproject.privideo.domain.user.dto.enums.GenderType;
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
    @EnumConstraint(enumClass = GenderType.class, message = "성별의 유형은 MALE 또는 FEMALE 이어야 합니다.")
    private String gender;

    @AgeTypeConstraint
    private Integer age;

    @NotBlank(message = "전화번호를 입력해주세요.")
    private String phoneNumber;

    private String organizationCode;
}
