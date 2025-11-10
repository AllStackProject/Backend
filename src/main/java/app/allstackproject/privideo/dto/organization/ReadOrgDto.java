package app.allstackproject.privideo.dto.organization;

import app.allstackproject.privideo.common.enumStatus.JoinStatusType;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class ReadOrgDto extends ReadOrgResult {
    private String code;

    public ReadOrgDto(Long id, String name, String imgUrl, LocalDateTime joinAt, Boolean isSuperAdmin, Boolean isAdmin,
                      JoinStatusType joinStatus, String code) {
        super(id, name, imgUrl, joinAt, isSuperAdmin, isAdmin, joinStatus);
        this.code = code;
    }
}
