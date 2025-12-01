package app.allstackproject.privideo.domain.organization.dto.request;

import app.allstackproject.privideo.global.annotation.EnumConstraint;
import app.allstackproject.privideo.domain.organization.dto.enums.JoinStatusType;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeJoinStateRequest {
    @EnumConstraint(enumClass = JoinStatusType.class, message = "가입 상태는 APPROVED 또는 REJECTED 이어야 합니다.")
    private String status;

    @NotNull
    private List<Long> memberGroupIds;
}
