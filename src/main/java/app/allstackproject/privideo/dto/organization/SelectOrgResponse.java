package app.allstackproject.privideo.dto.organization;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SelectOrgResponse {
    private String nickname;

    private SelectOrgResponse(String nickname) {
        this.nickname = nickname;
    }

    public static SelectOrgResponse of(String nickname) {
        return new SelectOrgResponse(nickname);
    }
}
