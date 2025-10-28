package app.allstackproject.privideo.dto.organization;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateOrgResponse {
    private Long orgId;

    private String orgCode;

    public static CreateOrgResponse of(CreateOrgResult createOrgResult) {
        return new CreateOrgResponse(createOrgResult.getOrgId(), createOrgResult.getOrgCode());
    }
}
