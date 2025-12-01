package app.allstackproject.privideo.domain.admin.dto;

import app.allstackproject.privideo.domain.organization.dto.enums.OpenScopeType;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateNoticeRequest {
    @NotNull
    private String title;

    @NotNull
    private String content;

    @NotNull
    private OpenScopeType openScope;

    @NotNull
    private List<Long> memberGroups;
}
