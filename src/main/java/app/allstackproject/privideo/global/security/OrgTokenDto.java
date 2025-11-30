package app.allstackproject.privideo.global.security;

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
