package app.allstackproject.privideo.service.admin;

import static app.allstackproject.privideo.common.enumStatus.JoinStatusType.APPROVED;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.CREATOR_CANNOT_CHANGE;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.FORBIDDEN_NO_PERMISSION;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.MEMBER_NOT_FOUND;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.ORGANIZATION_NOT_FOUND;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.USER_NOT_FOUND;

import app.allstackproject.privideo.common.enumStatus.JoinStatusType;
import app.allstackproject.privideo.common.enumStatus.PermissionType;
import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.common.jwt.JwtProvider;
import app.allstackproject.privideo.common.util.OrgCodeGenerator;
import app.allstackproject.privideo.common.util.RedisRetryUtil;
import app.allstackproject.privideo.dto.organization.ChangeJoinStateRequest;
import app.allstackproject.privideo.dto.organization.OrgCodeResponse;
import app.allstackproject.privideo.dto.organization.OrgTokenDto;
import app.allstackproject.privideo.dto.organization.UpdateMemberPermissionRequest;
import app.allstackproject.privideo.entity.Member;
import app.allstackproject.privideo.repository.member.MemberRepository;
import app.allstackproject.privideo.repository.organization.OrgRedisRepository;
import app.allstackproject.privideo.repository.organization.OrganizationRepository;
import app.allstackproject.privideo.repository.user.UserRepository;
import app.allstackproject.privideo.service.permission.PermissionService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {

    private final MemberRepository memberRepository;
    private final OrganizationRepository organizationRepository;
    private final OrgRedisRepository orgRedisRepository;
    private final UserRepository userRepository;

    private final JwtProvider jwtProvider;

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

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        joinRedisSysnc(orgId, targetMemberId, targetStatus,
                                targetMember.getPermissionCode());
                    }
                }
        );

        return true;
    }

    private void joinRedisSysnc(Long orgId, Long memberId, JoinStatusType newStatus,
                                long permissionCode) {

        try {
            if (newStatus == JoinStatusType.APPROVED) {
                orgRedisRepository.saveMemberPermission(orgId, memberId, permissionCode);
            } else if (newStatus == JoinStatusType.REJECTED) {
                orgRedisRepository.deleteMemberPermission(orgId, memberId);
            }
        } catch (Exception e) {
            log.error("Redis 동기화 실패");
        }

    }

    public boolean updateMemberPermission(Long adminUserId, Long orgId, Long targetMemberId,
                                          UpdateMemberPermissionRequest.PermissionMap permissionMap) {
        if (!organizationRepository.existsById(orgId)) {
            throw new ApiException(ORGANIZATION_NOT_FOUND);
        }

        Member admin = memberRepository.findByIdAndOrganizationId(adminUserId, orgId)
                .orElseThrow(() -> new ApiException(MEMBER_NOT_FOUND));

        if (!admin.isAdmin()) {
            throw new ApiException(FORBIDDEN_NO_PERMISSION);
        }

        Member targetMember = memberRepository.findByUserIdAndOrganizationId(targetMemberId, orgId)
                .orElseThrow(() -> new ApiException(MEMBER_NOT_FOUND));

        if (targetMember.isAdmin() && targetMember.getOrganization().getCreator().getId()
                .equals(targetMember.getUser().getId())) {
            throw new ApiException(CREATOR_CANNOT_CHANGE);
        }

        PermissionType[] newPermissions = convertToPermissionTypes(permissionMap);
        targetMember.replaceWith(newPermissions);

        long newPermissionCode = targetMember.getPermissionCode();

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        orgRedisRepository.saveMemberPermission(orgId, targetMemberId, newPermissionCode);
                    }
                }
        );
        return true;
    }

    private PermissionType[] convertToPermissionTypes(
            UpdateMemberPermissionRequest.PermissionMap permissionMap) {

        List<PermissionType> permissionList = new ArrayList<>();

        if (Boolean.TRUE.equals(permissionMap.getVideoQuizManage())) {
            permissionList.add(PermissionType.VIDEO_QUIZ_MANAGE);
        }
        if (Boolean.TRUE.equals(permissionMap.getStatsReport())) {
            permissionList.add(PermissionType.STATS_REPORT);
        }
        if (Boolean.TRUE.equals(permissionMap.getNotice())) {
            permissionList.add(PermissionType.NOTICE);
        }
        if (Boolean.TRUE.equals(permissionMap.getOrgSetting())) {
            permissionList.add(PermissionType.ORG_SETTING);
        }

        return permissionList.toArray(new PermissionType[0]);
    }

    @Transactional
    public OrgCodeResponse regenerateOrgCode(Long userId, Long orgId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(USER_NOT_FOUND));

        organizationRepository.findById(orgId)
                .orElseThrow(() -> new ApiException(ORGANIZATION_NOT_FOUND));

        Member member = memberRepository.findByUserIdAndOrganizationId(userId, orgId)
                .orElseThrow(() -> new ApiException(MEMBER_NOT_FOUND));

        if (member.getJoinStatus() != APPROVED) {
            throw new ApiException(MEMBER_NOT_FOUND);
        }

        String newCode = OrgCodeGenerator.generateCode(orgId);

        orgRedisRepository.regenerateCode(orgId, newCode);

        return new OrgCodeResponse(newCode);
    }
}
