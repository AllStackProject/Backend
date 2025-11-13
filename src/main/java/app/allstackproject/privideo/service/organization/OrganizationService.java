package app.allstackproject.privideo.service.organization;

import static app.allstackproject.privideo.common.enumStatus.BaseStatusType.ACTIVE;
import static app.allstackproject.privideo.common.enumStatus.JoinStatusType.APPROVED;
import static app.allstackproject.privideo.common.enumStatus.JoinStatusType.PENDING;
import static app.allstackproject.privideo.common.enumStatus.JoinStatusType.REJECTED;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.ALREADY_APPROVED_MEMBER;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.ALREADY_REQUESTED_MEMBER;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.MEMBER_NOT_FOUND;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.MEMBER_NOT_IN_ORGANIZATION;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.ORGANIZATION_NOT_FOUND;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.ORG_CODE_NOT_AVAILABLE;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.USER_NOT_FOUND;
import static app.allstackproject.privideo.common.util.OrgCodeGenerator.generateCode;

import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.common.jwt.JwtProvider;
import app.allstackproject.privideo.dto.organization.CreatOrgResult;
import app.allstackproject.privideo.dto.organization.CreateOrgRequest;
import app.allstackproject.privideo.dto.organization.OrgTokenDto;
import app.allstackproject.privideo.dto.organization.ReadOrgDto;
import app.allstackproject.privideo.dto.organization.ReadOrgResult;
import app.allstackproject.privideo.dto.organization.SelectOrgResult;
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
import java.util.Map;
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

    public CreatOrgResult createOrg(Long userId, @Valid CreateOrgRequest createOrgRequest, String imgUrl) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ApiException(USER_NOT_FOUND));
        Organization organization = Organization.create(user, createOrgRequest.getName(), imgUrl,
                createOrgRequest.getDesc());

        Member member = Member.create(user, organization, createOrgRequest.getNickname(), true, APPROVED);
        member.adminPermissionSet();

        organizationRepository.save(organization);
        memberRepository.save(member);

        String code = generateCode(user.getId());
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

        Long orgId = organization.getId();
        return new CreatOrgResult(orgId,
                jwtProvider.createOrgToken(OrgTokenDto.builder()
                        .userId(userId)
                        .memberId(member.getId())
                        .orgId(orgId)
                        .orgJoinStatus(member.getJoinStatus().toString())
                        .orgIsAdmin(member.isAdmin())
                        .orgPermission(member.getPermissionCode())
                        .build()));
    }

    @Transactional(readOnly = true)
    public boolean validateOrgName(Long userId, String orgName) {
        if (!userRepository.existsById(userId)) {
            throw new ApiException(USER_NOT_FOUND);
        }

        if (organizationRepository.findByNameAndStatus(orgName, ACTIVE).isPresent()) {
            return false;
        }

        return true;
    }

    @Transactional(readOnly = true)
    public List<ReadOrgDto> readOrgs(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ApiException(USER_NOT_FOUND);
        }

        List<ReadOrgResult> rows = organizationRepository.findAllByUserId(userId);
        List<Long> orgIds = rows.stream().map(ReadOrgResult::getId).toList();

        Map<Long, String> codeMap = orgRedisRepository.getOrgCodesByIds(orgIds);

        return rows.stream()
                .map(r -> new ReadOrgDto(
                        r.getId(),
                        r.getName(),
                        r.getImgUrl(),
                        r.getJoinAt(),
                        r.getIsSuperAdmin(),
                        r.getIsAdmin(),
                        r.getJoinStatus(),
                        codeMap.get(r.getId())
                ))
                .toList();
    }

    public boolean joinOrg(Long userId, String orgCode, String nickname) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(USER_NOT_FOUND));

        Long orgId = orgRedisRepository.getOrgIdByCode(orgCode);

        Organization organization = null;
        if (orgId != null) {
            organization = organizationRepository.findById(orgId)
                    .orElseThrow(() -> new ApiException(ORGANIZATION_NOT_FOUND));
        } else {
//            organization = orgRedisRepository.findByCode(orgCode)
//                    .orElseThrow(() -> new ApiException(INVALID_ORG_CODE));
//
//            orgId = organization.getId();
//
//            orgRedisRepository.saveOrgCode(orgId, orgCode);
            throw new ApiException(ORG_CODE_NOT_AVAILABLE);
        }

        Optional<Member> existMember = memberRepository.findByUserIdAndOrganizationIdAndStatus(userId, orgId, ACTIVE);

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
            Member newMember = Member.create(user, organization, nickname, false, PENDING);
            memberRepository.save(newMember);
        }

        return true;
    }

    @Transactional(readOnly = true)
    public boolean validateOrgNickname(Long userId, String nickname, String code) {
        if (!userRepository.existsById(userId)) {
            throw new ApiException(USER_NOT_FOUND);
        }

        Long orgId = orgRedisRepository.getOrgIdByCode(code);
        if (orgId == null) {
            throw new ApiException(ORG_CODE_NOT_AVAILABLE);
        }

        if (memberRepository.findByOrganizationIdAndNicknameAndStatus(orgId, nickname, ACTIVE).isPresent()) {
            return false;
        }

        return true;
    }

    @Transactional(readOnly = true)
    public SelectOrgResult selectOrg(Long userId, Long orgId) {
        userRepository.findById(userId).orElseThrow(() -> new ApiException(USER_NOT_FOUND));
        organizationRepository.findById(orgId)
                .orElseThrow(() -> new ApiException(ORGANIZATION_NOT_FOUND));
        Member member = memberRepository.findByUserIdAndOrganizationIdAndStatus(userId, orgId, ACTIVE)
                .orElseThrow(() -> new ApiException(MEMBER_NOT_FOUND));

        if (member.getJoinStatus() != APPROVED) {
            throw new ApiException(MEMBER_NOT_FOUND);
        }

        long latestPerm = permissionService.getMemberPermission(orgId, member.getId());

        return new SelectOrgResult(
                jwtProvider.createOrgToken(OrgTokenDto.builder()
                        .userId(userId)
                        .memberId(member.getId())
                        .orgId(orgId)
                        .orgJoinStatus(member.getJoinStatus().toString())
                        .orgIsAdmin(member.isAdmin())
                        .orgPermission(latestPerm)
                        .build()),
                member.getNickname());
    }

    public boolean exitOrg(Long userId, Long orgId) {
        Member member = memberRepository.findByUserIdAndOrganizationIdAndStatus(userId, orgId, ACTIVE)
                .orElseThrow(() -> new ApiException(MEMBER_NOT_IN_ORGANIZATION));

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
