package app.allstackproject.privideo.dto.organization;

import app.allstackproject.privideo.common.enumStatus.JoinStatusType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReadOrgDto {
    private Long id;

    private String name;

    private String imgUrl;

    private String code;

    private LocalDateTime joinAt;

    private Boolean isSuperAdmin;
    
    private Boolean isAdmin;

    private JoinStatusType joinStatus;
}
