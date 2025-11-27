package app.allstackproject.privideo.dto.organization;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateMemberPermissionRequest {
    @NotNull
    private Boolean videoManage;

    @NotNull
    private Boolean statsReportManage;

    @NotNull
    private Boolean noticeManage;

    @NotNull
    private Boolean orgSettingManage;
}