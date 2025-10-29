package app.allstackproject.privideo.service.admin;

import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.FORBIDDEN_NO_PERMISSION;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.MEMBER_NOT_FOUND;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.ORGANIZATION_NOT_FOUND;

import app.allstackproject.privideo.common.enumStatus.JoinStatusType;
import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.dto.organization.ChangeJoinStateRequest;
import app.allstackproject.privideo.entity.Member;
import app.allstackproject.privideo.repository.member.MemberRepository;
import app.allstackproject.privideo.repository.organization.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {

    private final MemberRepository memberRepository;
    private final OrganizationRepository organizationRepository;

    public boolean changeJoinState(Long adminUserId, Long orgId, ChangeJoinStateRequest changeJoinStateRequest) {
        if (!organizationRepository.existsById(orgId)) {
            throw new ApiException(ORGANIZATION_NOT_FOUND);
        }
        Member admin = memberRepository.findByUserIdAndOrganizationId(adminUserId, orgId)
                .orElseThrow(() -> new ApiException(MEMBER_NOT_FOUND));
        if (!admin.isAdmin()) {
            throw new ApiException(FORBIDDEN_NO_PERMISSION);
        }

        Long targetMemberId = changeJoinStateRequest.getMemberId();
        JoinStatusType targetStatus = JoinStatusType.valueOf(changeJoinStateRequest.getStatus());

        Member targetMember = memberRepository.findByIdAndOrganizationId(targetMemberId, orgId)
                .orElseThrow(() -> new ApiException(MEMBER_NOT_FOUND));

        targetMember.changeJoinStatus(targetStatus);

        return true;
    }
}
