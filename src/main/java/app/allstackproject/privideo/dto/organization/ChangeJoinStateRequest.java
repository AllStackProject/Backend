package app.allstackproject.privideo.dto.organization;

import app.allstackproject.privideo.common.annotation.EnumConstraint;
import app.allstackproject.privideo.common.enumStatus.JoinStatusType;
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
