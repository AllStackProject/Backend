package app.allstackproject.privideo.repository.member.custom;

import app.allstackproject.privideo.common.enumStatus.BaseStatusType;
import app.allstackproject.privideo.dto.admin.ReadAllMemberDto;
import java.util.List;

public interface MemberRepositoryCustom {
    long inactivateAllByUserId(Long userId);

    List<ReadAllMemberDto> findByOrganizationIdAndStatus(Long orgId, BaseStatusType baseStatusType);
}
