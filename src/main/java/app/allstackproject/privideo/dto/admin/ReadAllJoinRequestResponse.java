package app.allstackproject.privideo.dto.admin;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReadAllJoinRequestResponse {
    private List<ReadAllJoinRequestItem> joinRequests;

    private List<MemberGroupItem> allMemberGroups;

    private ReadAllJoinRequestResponse(List<ReadAllJoinRequestItem> joinRequests,
                                       List<MemberGroupItem> allMemberGroups) {
        this.joinRequests = joinRequests;
        this.allMemberGroups = allMemberGroups;
    }

    public static ReadAllJoinRequestResponse of(List<ReadAllJoinRequestItem> joinRequests,
                                                List<MemberGroupItem> allMemberGroups) {
        return new ReadAllJoinRequestResponse(joinRequests, allMemberGroups);
    }
}
