package app.allstackproject.privideo.dto.organization;

import app.allstackproject.privideo.common.enumStatus.JoinStatusType;
import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class ReadOrgDto {
    private Long id;

    private String name;

    private String imgUrl;

    private String code;

    private LocalDateTime joinAt;

    private Boolean isAdmin;

    private JoinStatusType joinStatus;

    @QueryProjection
    public ReadOrgDto(Long id, String name, String imgUrl, String code,
                      LocalDateTime joinAt, boolean isAdmin, JoinStatusType joinStatus) {
        this.id = id;
        this.name = name;
        this.imgUrl = imgUrl;
        this.code = code;
        this.joinAt = joinAt;
        this.isAdmin = isAdmin;
        this.joinStatus = joinStatus;
    }
}
