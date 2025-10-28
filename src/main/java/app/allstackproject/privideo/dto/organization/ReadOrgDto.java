package app.allstackproject.privideo.dto.organization;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class ReadOrgDto {
    private Long id;

    private String name;

    private String img;

    private String code;

    private LocalDateTime joinAt;

    private boolean isAdmin;

    private boolean isActive;

    @QueryProjection
    public ReadOrgDto(Long id, String name, String img, String code,
                      LocalDateTime joinAt, boolean isAdmin, boolean isActive) {
        this.id = id;
        this.name = name;
        this.img = img;
        this.code = code;
        this.joinAt = joinAt;
        this.isAdmin = isAdmin;
        this.isActive = isActive;
    }
}
