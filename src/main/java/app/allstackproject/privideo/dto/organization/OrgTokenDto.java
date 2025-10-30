package app.allstackproject.privideo.dto.organization;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrgTokenDto {
    private Long userId;

    private Long memberId;

    private Long orgId;

    private String orgJoinStatus;

    private Boolean orgIsAdmin;

    private Long orgPermission;
}
