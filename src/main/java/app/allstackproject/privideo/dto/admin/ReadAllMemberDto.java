package app.allstackproject.privideo.dto.admin;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReadAllMemberDto {
    private Long id;

    private String userName;

    private String nickname;

    private Boolean isSuperAdmin;

    private Boolean isAdmin;

    private List<String> memberGroups;

    public ReadAllMemberDto(Long id, String userName, String nickname, Boolean isSuperAdmin, Boolean isAdmin) {
        this.id = id;
        this.userName = userName;
        this.nickname = nickname;
        this.isSuperAdmin = isSuperAdmin;
        this.isAdmin = isAdmin;
    }
    
    public void setMemberGroups(List<String> memberGroups) {
        this.memberGroups = List.copyOf(memberGroups);
    }
}
