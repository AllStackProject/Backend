package app.allstackproject.privideo.domain.video.dto.response;

import app.allstackproject.privideo.domain.organization.dto.enums.OpenScopeType;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReadVideoInfoResponse {
    private String title;

    private String description;

    private String thumbnailUrl;

    private Long watchCnt;

    private LocalDate expiredAt;

    private Boolean isComment;

    private OpenScopeType openScope;

    private List<VideoMemberGroupItem> memberGroups;

    private ReadVideoInfoResponse(String title, String description, String thumbnailUrl, Long watchCnt,
                                  LocalDate expiredAt, Boolean isComment, OpenScopeType openScope,
                                  List<VideoMemberGroupItem> memberGroups) {
        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.watchCnt = watchCnt;
        this.expiredAt = expiredAt;
        this.isComment = isComment;
        this.openScope = openScope;
        this.memberGroups = memberGroups;
    }

    public static ReadVideoInfoResponse of(
            String title,
            String description,
            String thumbnailUrl,
            Long watchCnt,
            LocalDate expiredAt,
            Boolean isComment,
            OpenScopeType openScope,
            List<VideoMemberGroupItem> memberGroups
    ) {
        return new ReadVideoInfoResponse(
                title, description, thumbnailUrl, watchCnt, expiredAt, isComment, openScope, memberGroups
        );
    }
}
