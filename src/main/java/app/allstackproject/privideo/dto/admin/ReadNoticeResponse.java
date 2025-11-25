package app.allstackproject.privideo.dto.admin;

import java.util.List;
import lombok.Getter;

@Getter
public class ReadNoticeResponse {
    private String title;

    private String content;

    private List<NoticeMemberGroupInfo> memberGroups;

    private ReadNoticeResponse(String title, String content, List<NoticeMemberGroupInfo> memberGroups) {
        this.title = title;
        this.content = content;
        this.memberGroups = memberGroups;
    }

    public static ReadNoticeResponse of(String title, String content, List<NoticeMemberGroupInfo> memberGroups) {
        return new ReadNoticeResponse(title, content, memberGroups);
    }
}
