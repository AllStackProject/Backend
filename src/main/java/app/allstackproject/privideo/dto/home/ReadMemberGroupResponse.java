package app.allstackproject.privideo.dto.home;

import app.allstackproject.privideo.dto.admin.ReadAllMemberGroupItem;
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
