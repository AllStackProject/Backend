package app.allstackproject.privideo.dto.organization;

import lombok.Getter;

@Getter
public class CreateOrgResponse {
    private Long id;

    private String code;

    private CreateOrgResponse(Long id, String code) {
        this.id = id;
        this.code = code;
    }

    public static CreateOrgResponse of(CreateOrgResult createOrgResult) {
        return new CreateOrgResponse(createOrgResult.getOrgId(), createOrgResult.getOrgCode());
    }
}
