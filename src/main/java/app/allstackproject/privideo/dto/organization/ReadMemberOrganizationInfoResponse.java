package app.allstackproject.privideo.dto.organization;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReadMemberOrganizationInfoResponse {
    private String orgName;

    private String orgCode;

    private String nickname;

    private Boolean isAdmin;

    private LocalDateTime joinedAt;

    private List<String> memberGroups;

    private ReadMemberOrganizationInfoResponse(String orgName, String orgCode, String nickname, Boolean isAdmin,
                                               LocalDateTime joinedAt, List<String> memberGroups) {
        this.orgName = orgName;
        this.orgCode = orgCode;
        this.nickname = nickname;
        this.isAdmin = isAdmin;
        this.joinedAt = joinedAt;
        this.memberGroups = memberGroups;
    }

    public static ReadMemberOrganizationInfoResponse of(String orgName, String orgCode, String nickname,
                                                        Boolean isAdmin, LocalDateTime joinedAt,
                                                        List<String> memberGroups) {
        return new ReadMemberOrganizationInfoResponse(orgName, orgCode, nickname, isAdmin, joinedAt, memberGroups);
    }

}
