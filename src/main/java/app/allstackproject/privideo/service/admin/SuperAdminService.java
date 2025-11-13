package app.allstackproject.privideo.service.admin;

import static app.allstackproject.privideo.common.enumStatus.BaseStatusType.ACTIVE;
import static app.allstackproject.privideo.common.enumStatus.JoinStatusType.PENDING;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.CREATOR_CANNOT_CHANGE;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.FORBIDDEN_NO_PERMISSION;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.INVALID_MEMBER_GROUP;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.MEMBER_NOT_FOUND;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.MEMBER_NOT_IN_ORGANIZATION;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.ORGANIZATION_NOT_FOUND;

import app.allstackproject.privideo.common.enumStatus.JoinStatusType;
import app.allstackproject.privideo.common.enumStatus.PermissionType;
import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.dto.admin.ReadAllJoinRequestItem;
import app.allstackproject.privideo.dto.admin.ReadAllMemberItem;
import app.allstackproject.privideo.dto.organization.ChangeJoinStateRequest;
import app.allstackproject.privideo.dto.organization.UpdateMemberPermissionRequest;
import app.allstackproject.privideo.entity.Member;
import app.allstackproject.privideo.entity.MemberGroup;
import app.allstackproject.privideo.entity.MemberGroupMapping;
import app.allstackproject.privideo.repository.member.MemberGroupMappingRepository;
import app.allstackproject.privideo.repository.member.MemberGroupRepository;
import app.allstackproject.privideo.repository.member.MemberRepository;
import app.allstackproject.privideo.repository.organization.OrgRedisRepository;
import app.allstackproject.privideo.repository.organization.OrganizationRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
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
public class SuperAdminService {

    private final MemberRepository memberRepository;
    private final MemberGroupRepository memberGroupRepository;
    private final MemberGroupMappingRepository memberGroupMappingRepository;
    private final OrganizationRepository organizationRepository;
    private final OrgRedisRepository orgRedisRepository;

    @Transactional(readOnly = true)
    public List<ReadAllMemberItem> readAllMember(Long orgId) {
        return memberRepository.findByOrganizationId(orgId);
    }

    public boolean updateMemberPermission(Long memberId, Long orgId, UpdateMemberPermissionRequest permissionMap) {
        Member member = memberRepository.findByUserIdAndOrganizationIdAndStatus(memberId, orgId, ACTIVE)
                .orElseThrow(() -> new ApiException(MEMBER_NOT_IN_ORGANIZATION));

        if (member.isAdmin()) {
            throw new ApiException(CREATOR_CANNOT_CHANGE);
        }

        PermissionType[] newPermissions = convertToPermissionTypes(permissionMap);
        member.replaceWith(newPermissions);

        long newPermissionCode = member.getPermissionCode();

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        orgRedisRepository.saveMemberPermission(orgId, memberId, newPermissionCode);
                    }
                }
        );
        return true;
    }

    public boolean modifyMemberGroup(Long memberId, Long orgId, List<Long> memberGroupIds) {
        Member member = memberRepository.findByUserIdAndOrganizationIdAndStatus(memberId, orgId, ACTIVE)
                .orElseThrow(() -> new ApiException(MEMBER_NOT_IN_ORGANIZATION));

        if (!memberGroupIds.isEmpty()) {
            long validGroupCount = memberGroupRepository.countByIdInAndOrganizationId(
                    memberGroupIds, orgId);

            if (validGroupCount != memberGroupIds.size()) {
                throw new ApiException(INVALID_MEMBER_GROUP);
            }
        }

        memberGroupMappingRepository.deleteByMemberId(member.getId());

        if (!memberGroupIds.isEmpty()) {
            List<MemberGroup> memberGroups = memberGroupRepository.findAllById(memberGroupIds);

            List<MemberGroupMapping> newMappings = memberGroups.stream()
                    .map(memberGroup -> MemberGroupMapping.create(member, memberGroup))
                    .collect(Collectors.toList());

            memberGroupMappingRepository.saveAll(newMappings);
        }

        return true;
    }

    public boolean changeJoinState(Long orgId, Long memberId, ChangeJoinStateRequest changeJoinStateRequest) {
        JoinStatusType targetStatus = JoinStatusType.valueOf(changeJoinStateRequest.getStatus());
        List<Long> memberGroupIds = changeJoinStateRequest.getMemberGroupIds();

        Member targetMember = memberRepository.findByIdAndOrganizationIdAndStatus(memberId, orgId, ACTIVE)
                .orElseThrow(() -> new ApiException(MEMBER_NOT_IN_ORGANIZATION));

        targetMember.changeJoinStatus(targetStatus);

        long validGroupCount = memberGroupRepository.countByIdInAndOrganizationId(memberGroupIds, orgId);

        if (validGroupCount != memberGroupIds.size()) {
            throw new ApiException(INVALID_MEMBER_GROUP);
        }

        List<MemberGroup> memberGroups = memberGroupRepository.findAllById(memberGroupIds);
        List<MemberGroupMapping> newMappings = memberGroups.stream()
                .map(memberGroup -> MemberGroupMapping.create(targetMember, memberGroup))
                .collect(Collectors.toList());

        memberGroupMappingRepository.saveAll(newMappings);

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        joinRedisSysnc(orgId, memberId, targetStatus,
                                targetMember.getPermissionCode());
                    }
                }
        );

        return true;
    }

    @Transactional(readOnly = true)
    public List<ReadAllJoinRequestItem> readAllJoinRequest(Long orgId) {
        return memberRepository.findByOrganizationIdAndJoinStatus(orgId, PENDING);
    }

    public boolean withdrawMember(Long orgId, Long memberId) {
        Member member = memberRepository.findByIdAndOrganizationIdAndStatus(memberId, orgId, ACTIVE)
                .orElseThrow(() -> new ApiException(MEMBER_NOT_IN_ORGANIZATION));

        try {
            orgRedisRepository.deleteMemberPermission(orgId, memberId);
            log.info("조직 탈퇴 - Redis 권한 삭제 완료, memberId: {}", member.getId());
        } catch (Exception e) {
            log.error("Redis 권한 삭제 실패");
        }

        member.updateToInactive();
        return true;
    }

    private PermissionType[] convertToPermissionTypes(
            UpdateMemberPermissionRequest permissionMap) {

        List<PermissionType> permissionList = new ArrayList<>();

        if (Boolean.TRUE.equals(permissionMap.getVideoManage())) {
            permissionList.add(PermissionType.VIDEO_MANAGE);
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
}
