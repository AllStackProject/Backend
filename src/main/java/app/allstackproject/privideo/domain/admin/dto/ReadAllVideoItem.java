package app.allstackproject.privideo.domain.admin.dto;

import app.allstackproject.privideo.domain.organization.dto.enums.OpenScopeType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class ReadAllVideoItem {
    private Long id;

    private String title;

    @Setter
    private String thumbnailUrl;

    private LocalDateTime createdAt;

    private LocalDate expiredAt;

    private OpenScopeType openScope;

    private Long viewCnt;

    public ReadAllVideoItem(Long id, String title, String thumbnailUrl, LocalDateTime createdAt, LocalDate expiredAt,
                            String openScope, Long viewCnt) {
        this.id = id;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.createdAt = createdAt;
        this.expiredAt = expiredAt;
        this.openScope = OpenScopeType.valueOf(openScope);
        this.viewCnt = viewCnt;
    }
}
