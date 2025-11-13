package app.allstackproject.privideo.dto.admin;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReadAllJoinRequestResponse {
    private List<ReadAllJoinRequestItem> joinRequests;

    private ReadAllJoinRequestResponse(List<ReadAllJoinRequestItem> joinRequests) {
        this.joinRequests = joinRequests;
    }

    public static ReadAllJoinRequestResponse of(List<ReadAllJoinRequestItem> joinRequests) {
        return new ReadAllJoinRequestResponse(joinRequests);
    }
}
