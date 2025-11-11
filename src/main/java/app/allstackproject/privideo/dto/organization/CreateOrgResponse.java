package app.allstackproject.privideo.dto.organization;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateOrgResponse {
    private Long id;

    private CreateOrgResponse(Long id) {
        this.id = id;
    }

    public static CreateOrgResponse of(Long id) {
        return new CreateOrgResponse(id);
    }
}
