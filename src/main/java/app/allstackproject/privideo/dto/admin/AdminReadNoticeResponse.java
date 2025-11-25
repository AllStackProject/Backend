package app.allstackproject.privideo.dto.admin;

import java.util.List;
import lombok.Getter;

@Getter
public class AdminReadNoticeResponse {
    private String title;

    private String content;

    private List<NoticeMemberGroupInfo> memberGroups;

    private AdminReadNoticeResponse(String title, String content, List<NoticeMemberGroupInfo> memberGroups) {
        this.title = title;
        this.content = content;
        this.memberGroups = memberGroups;
    }

    public static AdminReadNoticeResponse of(String title, String content, List<NoticeMemberGroupInfo> memberGroups) {
        return new AdminReadNoticeResponse(title, content, memberGroups);
    }
}
