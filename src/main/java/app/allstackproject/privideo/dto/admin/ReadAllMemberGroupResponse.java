package app.allstackproject.privideo.dto.admin;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReadAllMemberGroupResponse {
    private List<ReadAllMemberGroupItem> memberGroups;

    private ReadAllMemberGroupResponse(List<ReadAllMemberGroupItem> memberGroups) {
        this.memberGroups = memberGroups;
    }

    public static ReadAllMemberGroupResponse of(List<ReadAllMemberGroupItem> memberGroups) {
        return new ReadAllMemberGroupResponse(memberGroups);
    }
}
