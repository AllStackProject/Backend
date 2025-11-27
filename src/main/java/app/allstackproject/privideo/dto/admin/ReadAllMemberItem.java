package app.allstackproject.privideo.dto.admin;

import static app.allstackproject.privideo.common.enumStatus.PermissionType.NOTICE;
import static app.allstackproject.privideo.common.enumStatus.PermissionType.ORG_SETTING;
import static app.allstackproject.privideo.common.enumStatus.PermissionType.STATS_REPORT;
import static app.allstackproject.privideo.common.enumStatus.PermissionType.VIDEO_MANAGE;

import app.allstackproject.privideo.common.enumStatus.PermissionType;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReadAllMemberItem {
    private Long id;

    private String userName;

    private String nickname;

    private Boolean isSuperAdmin;

    private Boolean videoManage;

    private Boolean statsReportManage;

    private Boolean noticeManage;

    private Boolean orgSettingManage;

    private List<MemberGroupItem> memberGroups;

    public ReadAllMemberItem(Long id, String userName, String nickname, Boolean isSuperAdmin, Long permissionCode) {
        this.id = id;
        this.userName = userName;
        this.nickname = nickname;
        this.isSuperAdmin = isSuperAdmin;
        
        long mask = permissionCode != null ? permissionCode : 0L;

        this.videoManage = PermissionType.has(mask, VIDEO_MANAGE);
        this.statsReportManage = PermissionType.has(mask, STATS_REPORT);
        this.noticeManage = PermissionType.has(mask, NOTICE);
        this.orgSettingManage = PermissionType.has(mask, ORG_SETTING);
    }

    public void setMemberGroups(List<MemberGroupItem> memberGroups) {
        this.memberGroups = List.copyOf(memberGroups);
    }
}
