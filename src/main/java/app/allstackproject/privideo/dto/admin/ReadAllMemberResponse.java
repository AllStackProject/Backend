package app.allstackproject.privideo.dto.admin;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReadAllMemberResponse {
    private List<ReadAllMemberDto> members;

    private ReadAllMemberResponse(List<ReadAllMemberDto> members) {
        this.members = members;
    }

    public static ReadAllMemberResponse of(List<ReadAllMemberDto> members) {
        return new ReadAllMemberResponse(members);
    }
}
