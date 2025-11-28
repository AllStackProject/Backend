package app.allstackproject.privideo.dto.user;

import app.allstackproject.privideo.common.annotation.AgeTypeConstraint;
import app.allstackproject.privideo.common.annotation.EnumConstraint;
import app.allstackproject.privideo.common.enumStatus.GenderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserInfoRequest {
    @NotNull
    private String newPassword;

    @AgeTypeConstraint
    private Integer changedAge;

    @NotNull
    private String confirmPassword;

    @EnumConstraint(enumClass = GenderType.class, message = "성별의 유형은 MALE 또는 FEMALE 이어야 합니다.")
    private String changedGender;

    @NotBlank(message = "전화번호를 입력해주세요.")
    @Pattern(regexp = "^01[0-9]{8,9}$", message = "전화번호 형식이 올바르지 않습니다. (예:01012345678)")
    private String changedPhoneNum;
}
