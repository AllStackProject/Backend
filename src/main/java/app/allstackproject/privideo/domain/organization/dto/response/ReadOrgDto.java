package app.allstackproject.privideo.domain.organization.dto.response;

import app.allstackproject.privideo.domain.organization.dto.enums.JoinStatusType;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class ReadOrgDto extends ReadOrgResult {
    private String code;

    public ReadOrgDto(Long id, String name, String imgUrl, LocalDateTime joinAt, Boolean isSuperAdmin,
                      Boolean videoManage, Boolean statsReportManage, Boolean noticeManage, Boolean orgSettingManage,
                      JoinStatusType joinStatus, String code) {
        super(id, name, imgUrl, joinAt, isSuperAdmin, videoManage, statsReportManage, noticeManage, orgSettingManage,
                joinStatus);
        this.code = code;
    }
}
