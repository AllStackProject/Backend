package app.allstackproject.privideo.dto.organization;

import static app.allstackproject.privideo.domain.organization.dto.enums.PermissionType.NOTICE;
import static app.allstackproject.privideo.domain.organization.dto.enums.PermissionType.ORG_SETTING;
import static app.allstackproject.privideo.domain.organization.dto.enums.PermissionType.STATS_REPORT;
import static app.allstackproject.privideo.domain.organization.dto.enums.PermissionType.VIDEO_MANAGE;

import app.allstackproject.privideo.domain.organization.dto.enums.JoinStatusType;
import app.allstackproject.privideo.domain.organization.dto.enums.PermissionType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReadOrgResult {
    private Long id;

    private String name;

    private String imgUrl;

    private LocalDateTime joinAt;

    private Boolean isSuperAdmin;

    private Boolean videoManage;

    private Boolean statsReportManage;

    private Boolean noticeManage;

    private Boolean orgSettingManage;

    private JoinStatusType joinStatus;

    public ReadOrgResult(Long id,
                         String name,
                         String imgUrl,
                         LocalDateTime joinAt,
                         Boolean isSuperAdmin,
                         Long permissionCode,
                         JoinStatusType joinStatus) {
        this.id = id;
        this.name = name;
        this.imgUrl = imgUrl;
        this.joinAt = joinAt;
        this.isSuperAdmin = isSuperAdmin;
        this.joinStatus = joinStatus;

        long mask = permissionCode != null ? permissionCode : 0L;

        this.videoManage = PermissionType.has(mask, VIDEO_MANAGE);
        this.statsReportManage = PermissionType.has(mask, STATS_REPORT);
        this.noticeManage = PermissionType.has(mask, NOTICE);
        this.orgSettingManage = PermissionType.has(mask, ORG_SETTING);
    }
}
