package app.allstackproject.privideo.domain.member.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ModifyMemberGroupRequest {
    @NotNull
    private List<Long> memberGroupIds;
}
