package app.allstackproject.privideo.repository.member.custom;

import app.allstackproject.privideo.common.enumStatus.JoinStatusType;
import app.allstackproject.privideo.dto.admin.AgeCountDto;
import app.allstackproject.privideo.dto.admin.GenderCountDto;
import app.allstackproject.privideo.dto.admin.ReadAllJoinRequestItem;
import app.allstackproject.privideo.dto.admin.ReadAllMemberItem;
import java.util.List;

public interface MemberRepositoryCustom {
    long inactivateAllByUserId(Long userId);

    List<ReadAllMemberItem> findByOrganizationId(Long orgId);

    List<ReadAllJoinRequestItem> findByOrganizationIdAndJoinStatus(Long orgId, JoinStatusType joinStatus);

    List<GenderCountDto> countMemberByGender(Long orgId);

    List<AgeCountDto> countMemberByAge(Long orgId);
}
