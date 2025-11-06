package app.allstackproject.privideo.service.organization;

import static app.allstackproject.privideo.common.enumStatus.JoinStatusType.APPROVED;
import static app.allstackproject.privideo.common.enumStatus.JoinStatusType.PENDING;
import static app.allstackproject.privideo.common.enumStatus.JoinStatusType.REJECTED;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.ALREADY_APPROVED_MEMBER;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.ALREADY_REQUESTED_MEMBER;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.DUPLICATE_ORG_NAME;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.INVALID_ORG_CODE;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.MEMBER_NOT_FOUND;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.ORGANIZATION_NOT_FOUND;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.USER_NOT_FOUND;
import static app.allstackproject.privideo.common.util.OrgCodeGenerator.generateCode;


import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.common.jwt.JwtProvider;
import app.allstackproject.privideo.common.util.OrgCodeGenerator;
import app.allstackproject.privideo.dto.organization.CreateOrgRequest;
import app.allstackproject.privideo.dto.organization.CreateOrgResult;
import app.allstackproject.privideo.dto.organization.OrgTokenDto;
import app.allstackproject.privideo.dto.organization.ReadOrgDto;
import app.allstackproject.privideo.entity.Member;
import app.allstackproject.privideo.entity.Organization;
import app.allstackproject.privideo.entity.User;
import app.allstackproject.privideo.repository.member.MemberRepository;
import app.allstackproject.privideo.repository.organization.OrgRedisRepository;
import app.allstackproject.privideo.repository.organization.OrganizationRepository;
import app.allstackproject.privideo.repository.user.UserRepository;
import app.allstackproject.privideo.service.permission.PermissionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OrganizationService {

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final OrganizationRepository organizationRepository;
    private final OrgRedisRepository orgRedisRepository;
    private final PermissionService permissionService;

    private final JwtProvider jwtProvider;

    public CreateOrgResult createOrg(Long userId, @Valid CreateOrgRequest createOrgRequest) {
        if (organizationRepository.findByName(createOrgRequest.getName()).isPresent()) {
            throw new ApiException(DUPLICATE_ORG_NAME);
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new ApiException(USER_NOT_FOUND));

        String code = generateCode(user.getId());
        Organization organization = Organization.create(user, createOrgRequest.getName(), createOrgRequest.getImgUrl(),
                createOrgRequest.getDesc(), code);
        Member member = Member.create(user, organization, true, APPROVED);

        member.adminPermissionSet();

        organizationRepository.save(organization);
        memberRepository.save(member);

        try {
            orgRedisRepository.createOrgCode(organization.getId(), code);
            log.info("조직 코드 Redis 저장 완료 - orgId: {}, code: {}", organization.getId(), code);

            orgRedisRepository.saveMemberPermission(
                    organization.getId(),
                    member.getId(),
                    member.getPermissionCode()
            );

        } catch (Exception e) {
            log.info("Redis 저장 실패 - orgId: {}", organization.getId());
        }

        return new CreateOrgResult(organization.getId(), code);
    }

    @Transactional(readOnly = true)
    public List<ReadOrgDto> readOrgs(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new ApiException(USER_NOT_FOUND));
        return organizationRepository.findAllByUserId(userId);
    }


    public boolean joinOrg(Long userId, String orgCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(USER_NOT_FOUND));

        Long orgId = orgRedisRepository.getOrgIdByCode(orgCode);

        Organization organization = null;
        if (orgId == null) {
            organization = organizationRepository.findByCode(orgCode)
                    .orElseThrow(() -> new ApiException(INVALID_ORG_CODE));

            orgId = organization.getId();
            
            orgRedisRepository.saveOrgCode(orgId, orgCode);
        } else {
            organization = organizationRepository.findById(orgId)
                    .orElseThrow(() -> new ApiException(ORGANIZATION_NOT_FOUND));
        }

        Optional<Member> existMember = memberRepository.findByUserIdAndOrganizationId(userId, orgId);

        if (existMember.isPresent()) {
            Member member = existMember.get();

            if (member.getJoinStatus() == APPROVED) {
                throw new ApiException(ALREADY_APPROVED_MEMBER);
            }
            if (member.getJoinStatus() == PENDING) {
                throw new ApiException(ALREADY_REQUESTED_MEMBER);
            }
            if (member.getJoinStatus() == REJECTED) {
                member.changeJoinStatus(PENDING);
            }

        } else {
            Member newMember = Member.create(user, organization, false, PENDING);
            memberRepository.save(newMember);
        }

        return true;
    }

    @Transactional(readOnly = true)
    public String selectOrg(Long userId, Long orgId) {
        userRepository.findById(userId).orElseThrow(() -> new ApiException(USER_NOT_FOUND));
        organizationRepository.findById(orgId)
                .orElseThrow(() -> new ApiException(ORGANIZATION_NOT_FOUND));
        Member member = memberRepository.findByUserIdAndOrganizationId(userId, orgId)
                .orElseThrow(() -> new ApiException(MEMBER_NOT_FOUND));

        if (member.getJoinStatus() != APPROVED) {
            throw new ApiException(MEMBER_NOT_FOUND);
        }

        long latestPerm = permissionService.getMemberPermission(orgId, member.getId());

        return jwtProvider.createOrgToken(OrgTokenDto.builder()
                .userId(userId)
                .memberId(member.getId())
                .orgId(orgId)
                .orgJoinStatus(member.getJoinStatus().toString())
                .orgIsAdmin(member.isAdmin())
                .orgPermission(latestPerm)
                .build());

    }

    public boolean exitOrg(Long userId, Long orgId) {
        userRepository.findById(userId).orElseThrow(() -> new ApiException(USER_NOT_FOUND));
        organizationRepository.findById(orgId)
                .orElseThrow(() -> new ApiException(ORGANIZATION_NOT_FOUND));
        Member member = memberRepository.findByUserIdAndOrganizationId(userId, orgId)
                .orElseThrow(() -> new ApiException(MEMBER_NOT_FOUND));

        try {
            orgRedisRepository.deleteMemberPermission(orgId, member.getId());
            log.info("조직 탈퇴 - Redis 권한 삭제 완료, memberId: {}", member.getId());
        } catch (Exception e) {
            log.error("Redis 권한 삭제 실패");
        }

        member.updateToInactive();
        return true;
    }
}
