package app.allstackproject.privideo.dto.admin;

import app.allstackproject.privideo.common.enumStatus.VideoOpenScopeType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class ReadAllVideoDto {
    private Long id;

    private String title;

    private String thumbnailUrl;

    private LocalDateTime createdAt;

    private LocalDate expiredAt;

    private VideoOpenScopeType openScope;

    private Long viewCnt;

    public ReadAllVideoDto(Long id, String title, String thumbnailUrl, LocalDateTime createdAt, LocalDate expiredAt,
                           String openScope, Long viewCnt) {
        this.id = id;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.createdAt = createdAt;
        this.expiredAt = expiredAt;
        this.openScope = VideoOpenScopeType.valueOf(openScope);
        this.viewCnt = viewCnt;
    }
}
