package app.allstackproject.privideo.dto.organization;

import app.allstackproject.privideo.common.enumStatus.PermissionType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateMemberPermissionRequest {
    @NotNull
    private Long memberId;

    @NotNull
    private PermissionMap permissions;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class PermissionMap {
        @NotNull
        private Boolean videoQuizManage;

        @NotNull
        private Boolean statsReport;

        @NotNull
        private Boolean notice;

        @NotNull
        private Boolean orgSetting;

    }
}