package app.allstackproject.privideo.dto.admin;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReadAllMemberItem {
    private Long id;

    private String userName;

    private String nickname;

    private Boolean isSuperAdmin;

    private Boolean isAdmin;

    private List<MemberGroupItem> memberGroups;

    public ReadAllMemberItem(Long id, String userName, String nickname, Boolean isSuperAdmin, Boolean isAdmin) {
        this.id = id;
        this.userName = userName;
        this.nickname = nickname;
        this.isSuperAdmin = isSuperAdmin;
        this.isAdmin = isAdmin;
    }

    public void setMemberGroups(List<MemberGroupItem> memberGroups) {
        this.memberGroups = List.copyOf(memberGroups);
    }
}
