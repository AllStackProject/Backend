package app.allstackproject.privideo.domain.member.dto.response;

import app.allstackproject.privideo.domain.admin.dto.ReadAllMemberGroupItem;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReadMemberGroupResponse {
    private List<ReadAllMemberGroupItem> memberGroups;

    private ReadMemberGroupResponse(List<ReadAllMemberGroupItem> memberGroups) {
        this.memberGroups = memberGroups;
    }

    public static ReadMemberGroupResponse of(List<ReadAllMemberGroupItem> memberGroups) {
        return new ReadMemberGroupResponse(memberGroups);
    }
}
