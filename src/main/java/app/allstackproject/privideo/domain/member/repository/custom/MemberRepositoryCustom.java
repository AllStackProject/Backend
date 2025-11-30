package app.allstackproject.privideo.domain.member.repository.custom;

import app.allstackproject.privideo.domain.organization.dto.enums.JoinStatusType;
import app.allstackproject.privideo.domain.admin.dto.AgeCountDto;
import app.allstackproject.privideo.domain.admin.dto.GenderCountDto;
import app.allstackproject.privideo.domain.admin.dto.ReadAllJoinRequestItem;
import app.allstackproject.privideo.domain.admin.dto.ReadAllMemberItem;
import java.util.List;

public interface MemberRepositoryCustom {
    long inactivateAllByUserId(Long userId);

    List<ReadAllMemberItem> findByOrganizationId(Long orgId);

    List<ReadAllJoinRequestItem> findByOrganizationIdAndJoinStatus(Long orgId, JoinStatusType joinStatus);

    List<GenderCountDto> countMemberByGender(Long orgId);

    List<AgeCountDto> countMemberByAge(Long orgId);
}
