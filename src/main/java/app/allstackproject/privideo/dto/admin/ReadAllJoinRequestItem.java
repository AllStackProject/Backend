package app.allstackproject.privideo.dto.admin;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;

@Getter
public class ReadAllJoinRequestItem {
    private Long id;

    private String userName;

    private String nickname;

    private LocalDateTime requestedAt;

    private List<MemberGroupItem> memberGroups;

    public ReadAllJoinRequestItem(Long id, String userName, String nickname, LocalDateTime requestedAt) {
        this.id = id;
        this.userName = userName;
        this.nickname = nickname;
        this.requestedAt = requestedAt;
    }

    public void setMemberGroups(List<MemberGroupItem> memberGroups) {
        this.memberGroups = List.copyOf(memberGroups);
    }
}
