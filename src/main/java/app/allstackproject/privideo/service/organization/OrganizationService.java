package app.allstackproject.privideo.service.organization;

import static app.allstackproject.privideo.common.enumStatus.JoinStatusType.APPROVED;
import static app.allstackproject.privideo.common.enumStatus.JoinStatusType.PENDING;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.DUPLICATE_ORG_NAME;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.INVALID_ORG_CODE;
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
import app.allstackproject.privideo.repository.organization.OrgRedisRepository;
import app.allstackproject.privideo.repository.organization.OrganizationRepository;
import app.allstackproject.privideo.repository.user.UserRepository;
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

        organizationRepository.save(organization);
        memberRepository.save(member);

        try {
            orgRedisRepository.createOrgCode(organization.getId(), code);
            log.info("조직 코드 Redis 저장 완료 - orgId: {}, code: {}", organization.getId(), code);
        } catch (Exception e) {
            //Redis 장애
            log.info("조직 코드 Redis 저장 완료 - orgId: {}, code: {}", organization.getId(), code);
        }

        return new CreateOrgResult(organization.getId(), code);
    }

    @Transactional(readOnly = true)
    public List<ReadOrgDto> readOrgs(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new ApiException(USER_NOT_FOUND));
        return organizationRepository.findAllByUserId(userId);
    }

    public boolean joinOrg(Long userId, Long orgId, String orgCode) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ApiException(USER_NOT_FOUND));
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ApiException(ORGANIZATION_NOT_FOUND));

        if (!organization.getCode().equals(orgCode)) {
            throw new ApiException(INVALID_ORG_CODE);
        }

        Optional<Member> member = memberRepository.findByUserIdAndOrganizationId(userId, organization.getId());

        if (member.isPresent()) {
            // TODO: pending -> [이미 요청을 보냈습니다] 예외, approved -> [이미 속한 조직입니다] 예외, rejected -> 기존 엔티티를 request로 수정
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

        //TODO
        //request 상태인지 rejected 상태인지 pending도 안 됨 approved일 때만 선택 가능
        //approve고 선택 한 경우 -> redis 권한 변경

        return jwtProvider.createOrgToken(OrgTokenDto.builder()
                .userId(userId)
                .memberId(member.getId())
                .orgId(orgId)
                .orgJoinStatus(member.getJoinStatus().toString())
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
