package app.allstackproject.privideo.service.organization;

import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.DUPLICATE_ORG_NAME;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.MEMBER_NOT_FOUND;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.ORGANIZATION_NOT_FOUND;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.USER_NOT_FOUND;
import static app.allstackproject.privideo.common.util.OrgCodeGenerator.generateCode;

import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.common.jwt.JwtProvider;
import app.allstackproject.privideo.dto.organization.CreateOrgRequest;
import app.allstackproject.privideo.dto.organization.CreateOrgResult;
import app.allstackproject.privideo.dto.organization.OrgTokenDto;
import app.allstackproject.privideo.dto.organization.ReadOrgDto;
import app.allstackproject.privideo.entity.Member;
import app.allstackproject.privideo.entity.Organization;
import app.allstackproject.privideo.entity.User;
import app.allstackproject.privideo.repository.member.MemberRepository;
import app.allstackproject.privideo.repository.organization.OrganizationRepository;
import app.allstackproject.privideo.repository.user.UserRepository;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class OrganizationService {

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final OrganizationRepository organizationRepository;
    private final JwtProvider jwtProvider;

    public CreateOrgResult createOrg(Long userId, @Valid CreateOrgRequest createOrgRequest) {
        if (organizationRepository.findByName(createOrgRequest.getName()).isPresent()) {
            throw new ApiException(DUPLICATE_ORG_NAME);
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new ApiException(USER_NOT_FOUND));

        String code = generateCode(user.getId());
        Organization organization = Organization.create(user, createOrgRequest.getName(), createOrgRequest.getImgUrl(),
                createOrgRequest.getDesc(), code);
        Member member = Member.create(user, organization, true, true);

        organizationRepository.save(organization);
        memberRepository.save(member);

        return new CreateOrgResult(organization.getId(), code);
    }

    @Transactional(readOnly = true)
    public List<ReadOrgDto> readOrgs(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new ApiException(USER_NOT_FOUND));
        return organizationRepository.findAllByUserId(userId);
    }

    public boolean joinOrg(Long userId, String orgName, String orgCode) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ApiException(USER_NOT_FOUND));
        Organization organization = organizationRepository.findByNameAndCode(orgName, orgCode)
                .orElseThrow(() -> new ApiException(ORGANIZATION_NOT_FOUND));

        Member member = Member.create(user, organization, false, false);
        memberRepository.save(member);
        return true;
    }

    @Transactional(readOnly = true)
    public String selectOrg(Long userId, Long orgId) {
        userRepository.findById(userId).orElseThrow(() -> new ApiException(USER_NOT_FOUND));
        organizationRepository.findById(orgId)
                .orElseThrow(() -> new ApiException(ORGANIZATION_NOT_FOUND));
        Member member = memberRepository.findByUserIdAndOrganizationId(userId, orgId)
                .orElseThrow(() -> new ApiException(MEMBER_NOT_FOUND));

        return jwtProvider.createOrgToken(OrgTokenDto.builder()
                .userId(userId)
                .memberId(member.getId())
                .orgId(orgId)
                .orgIsAdmin(member.isAdmin())
                .orgPermission(member.getPermissionCode())
                .build());
    }

    public boolean exitOrg(Long userId, Long orgId) {
        userRepository.findById(userId).orElseThrow(() -> new ApiException(USER_NOT_FOUND));
        organizationRepository.findById(orgId)
                .orElseThrow(() -> new ApiException(ORGANIZATION_NOT_FOUND));
        Member member = memberRepository.findByUserIdAndOrganizationId(userId, orgId)
                .orElseThrow(() -> new ApiException(MEMBER_NOT_FOUND));

        member.updateToInactive();
        return false;
    }
}
