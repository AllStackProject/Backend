package app.allstackproject.privideo.domain.admin.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReadAllMemberResponse {
    private List<ReadAllMemberItem> members;

    private ReadAllMemberResponse(List<ReadAllMemberItem> members) {
        this.members = members;
    }

    public static ReadAllMemberResponse of(List<ReadAllMemberItem> members) {
        return new ReadAllMemberResponse(members);
    }
}
