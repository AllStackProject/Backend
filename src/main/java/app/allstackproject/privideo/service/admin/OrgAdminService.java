package app.allstackproject.privideo.service.admin;

import static app.allstackproject.privideo.common.enumStatus.BaseStatusType.ACTIVE;
import static app.allstackproject.privideo.common.enumStatus.JoinStatusType.APPROVED;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.MEMBER_GROUP_ALREADY_EXIST;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.MEMBER_GROUP_NOT_FOUND;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.MEMBER_NOT_FOUND;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.ORGANIZATION_NOT_FOUND;

import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.common.util.OrgCodeGenerator;
import app.allstackproject.privideo.dto.organization.OrgCodeResponse;
import app.allstackproject.privideo.entity.Member;
import app.allstackproject.privideo.entity.MemberGroup;
import app.allstackproject.privideo.entity.Organization;
import app.allstackproject.privideo.repository.member.MemberGroupRepository;
import app.allstackproject.privideo.repository.member.MemberRepository;
import app.allstackproject.privideo.repository.organization.OrgRedisRepository;
import app.allstackproject.privideo.repository.organization.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class OrgAdminService {

    private final MemberRepository memberRepository;
    private final OrganizationRepository organizationRepository;
    private final OrgRedisRepository orgRedisRepository;
    private final MemberGroupRepository memberGroupRepository;

    public OrgCodeResponse regenerateOrgCode(Long memberId, Long orgId) {
        organizationRepository.findById(orgId)
                .orElseThrow(() -> new ApiException(ORGANIZATION_NOT_FOUND));

        Member member = memberRepository.findByIdAndOrganizationIdAndStatus(memberId, orgId, ACTIVE)
                .orElseThrow(() -> new ApiException(MEMBER_NOT_FOUND));

        if (member.getJoinStatus() != APPROVED) {
            throw new ApiException(MEMBER_NOT_FOUND);
        }

        String newCode = OrgCodeGenerator.generateCode(orgId);

        orgRedisRepository.regenerateCode(orgId, newCode);

        return new OrgCodeResponse(newCode);
    }

    public boolean createMemberGroup(Long orgId, String name) {
        if (memberGroupRepository.existsByName(name)) {
            throw new ApiException(MEMBER_GROUP_ALREADY_EXIST);
        }

        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ApiException(ORGANIZATION_NOT_FOUND));
        memberGroupRepository.save(MemberGroup.create(organization, name));
        return true;
    }

    public boolean deleteMemberGroup(Long orgId, Long groupId) {
        MemberGroup memberGroup = memberGroupRepository.findByIdAndOrganizationId(groupId, orgId)
                .orElseThrow(() -> new ApiException(MEMBER_GROUP_NOT_FOUND));
        memberGroupRepository.delete(memberGroup);
        return true;
    }
}
