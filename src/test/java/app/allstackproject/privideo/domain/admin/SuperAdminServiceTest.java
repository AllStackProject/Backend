package app.allstackproject.privideo.domain.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.allstackproject.privideo.domain.admin.dto.MemberGroupItem;
import app.allstackproject.privideo.domain.admin.dto.ReadAllJoinRequestItem;
import app.allstackproject.privideo.domain.admin.dto.ReadAllJoinRequestResponse;
import app.allstackproject.privideo.domain.admin.dto.ReadAllMemberItem;
import app.allstackproject.privideo.domain.admin.service.SuperAdminService;
import app.allstackproject.privideo.domain.member.entity.Member;
import app.allstackproject.privideo.domain.member.entity.MemberGroup;
import app.allstackproject.privideo.domain.member.repository.MemberGroupMappingRepository;
import app.allstackproject.privideo.domain.member.repository.MemberGroupRepository;
import app.allstackproject.privideo.domain.member.repository.MemberRepository;
import app.allstackproject.privideo.domain.organization.dto.enums.JoinStatusType;
import app.allstackproject.privideo.domain.organization.dto.enums.PermissionType;
import app.allstackproject.privideo.domain.organization.dto.request.ChangeJoinStateRequest;
import app.allstackproject.privideo.domain.organization.dto.request.UpdateMemberPermissionRequest;
import app.allstackproject.privideo.domain.organization.entity.Organization;
import app.allstackproject.privideo.domain.organization.repository.OrgRedisRepository;
import app.allstackproject.privideo.domain.organization.repository.OrganizationRepository;
import app.allstackproject.privideo.domain.video.repository.CategoryRepository;
import app.allstackproject.privideo.domain.video.repository.VideoRepository;
import app.allstackproject.privideo.global.exception.ApiException;
import app.allstackproject.privideo.shared.enums.BaseStatusType;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@ExtendWith(MockitoExtension.class)
class SuperAdminServiceTest {

    @Mock
    MemberRepository memberRepository;
    @Mock
    MemberGroupRepository memberGroupRepository;
    @Mock
    MemberGroupMappingRepository memberGroupMappingRepository;
    @Mock
    OrganizationRepository organizationRepository;
    @Mock
    OrgRedisRepository orgRedisRepository;
    @Mock
    VideoRepository videoRepository;
    @Mock
    CategoryRepository categoryRepository;

    @InjectMocks
    SuperAdminService superAdminService;

    @AfterEach
    void clearSync() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    @DisplayName("readAllMember - 조직 구성원 목록 조회")
    void readAllMember_success() {
        Long orgId = 1L;
        ReadAllMemberItem item = mock(ReadAllMemberItem.class);
        when(memberRepository.findByOrganizationId(orgId))
                .thenReturn(List.of(item));

        List<ReadAllMemberItem> result = superAdminService.readAllMember(orgId);

        assertEquals(1, result.size());
        assertSame(item, result.get(0));
    }

    @Test
    @DisplayName("updateMemberPermission - 조직에 속하지 않으면 예외")
    void updateMemberPermission_memberNotInOrg() {
        Long orgId = 1L;
        Long memberId = 10L;
        UpdateMemberPermissionRequest req = mock(UpdateMemberPermissionRequest.class);

        when(memberRepository.findByIdAndOrganizationIdAndStatus(memberId, orgId, BaseStatusType.ACTIVE))
                .thenReturn(java.util.Optional.empty());

        assertThrows(ApiException.class,
                () -> superAdminService.updateMemberPermission(memberId, orgId, req));
    }

    @Test
    @DisplayName("updateMemberPermission - creator(관리자)이면 권한 변경 불가")
    void updateMemberPermission_creatorCannotChange() {
        Long orgId = 1L;
        Long memberId = 10L;

        Member member = mock(Member.class);
        when(memberRepository.findByIdAndOrganizationIdAndStatus(memberId, orgId, BaseStatusType.ACTIVE))
                .thenReturn(java.util.Optional.of(member));
        when(member.isAdmin()).thenReturn(true);

        UpdateMemberPermissionRequest req = mock(UpdateMemberPermissionRequest.class);

        assertThrows(ApiException.class,
                () -> superAdminService.updateMemberPermission(memberId, orgId, req));

        verify(orgRedisRepository, never()).saveMemberPermission(anyLong(), anyLong(), anyLong());
    }

    @Test
    @DisplayName("updateMemberPermission - 권한 변경 후 커밋 시 Redis 반영")
    void updateMemberPermission_success_syncRedisAfterCommit() {
        Long orgId = 1L;
        Long memberId = 10L;

        TransactionSynchronizationManager.initSynchronization();

        Member member = mock(Member.class);
        when(memberRepository.findByIdAndOrganizationIdAndStatus(memberId, orgId, BaseStatusType.ACTIVE))
                .thenReturn(java.util.Optional.of(member));
        when(member.isAdmin()).thenReturn(false);
        when(member.getPermissionCode()).thenReturn(7L);

        UpdateMemberPermissionRequest req = mock(UpdateMemberPermissionRequest.class);
        when(req.getVideoManage()).thenReturn(true);
        when(req.getNoticeManage()).thenReturn(true);
        when(req.getStatsReportManage()).thenReturn(false);
        when(req.getOrgSettingManage()).thenReturn(false);

        boolean result = superAdminService.updateMemberPermission(memberId, orgId, req);

        assertTrue(result);
        verify(member).replaceWith(any(PermissionType[].class));

        // afterCommit 수동 호출
        for (TransactionSynchronization sync : TransactionSynchronizationManager.getSynchronizations()) {
            sync.afterCommit();
        }

        verify(orgRedisRepository).saveMemberPermission(orgId, memberId, 7L);
    }

    @Test
    @DisplayName("modifyMemberGroup - 유효하지 않은 그룹이 있으면 예외")
    void modifyMemberGroup_invalidGroup() {
        Long orgId = 1L;
        Long memberId = 10L;
        List<Long> groupIds = List.of(1L, 2L);

        Member member = mock(Member.class);
        when(memberRepository.findByIdAndOrganizationIdAndStatus(memberId, orgId, BaseStatusType.ACTIVE))
                .thenReturn(java.util.Optional.of(member));

        when(memberGroupRepository.countByIdInAndOrganizationId(groupIds, orgId))
                .thenReturn(1L); // 실제 2개 요청 → invalid

        assertThrows(ApiException.class,
                () -> superAdminService.modifyMemberGroup(memberId, orgId, groupIds));
    }

    @Test
    @DisplayName("modifyMemberGroup - 기존 매핑 삭제 후 새 매핑 저장")
    void modifyMemberGroup_success() {
        Long orgId = 1L;
        Long memberId = 10L;
        List<Long> groupIds = List.of(1L, 2L);

        Member member = mock(Member.class);
        when(memberRepository.findByIdAndOrganizationIdAndStatus(memberId, orgId, BaseStatusType.ACTIVE))
                .thenReturn(java.util.Optional.of(member));
        when(member.getId()).thenReturn(memberId);

        when(memberGroupRepository.countByIdInAndOrganizationId(groupIds, orgId))
                .thenReturn((long) groupIds.size());

        MemberGroup g1 = mock(MemberGroup.class);
        MemberGroup g2 = mock(MemberGroup.class);
        when(memberGroupRepository.findAllById(groupIds))
                .thenReturn(List.of(g1, g2));

        boolean result = superAdminService.modifyMemberGroup(memberId, orgId, groupIds);

        assertTrue(result);
        verify(memberGroupMappingRepository).deleteByMemberId(memberId);
        verify(memberGroupMappingRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("changeJoinState - APPROVED 아닌 경우 그룹 매핑/Redis 없이 상태만 변경")
    void changeJoinState_notApproved_onlyStatusChange() {
        Long orgId = 1L;
        Long memberId = 10L;

        ChangeJoinStateRequest req = mock(ChangeJoinStateRequest.class);
        when(req.getStatus()).thenReturn(JoinStatusType.REJECTED.name());
        when(req.getMemberGroupIds()).thenReturn(List.of());

        Member member = mock(Member.class);
        when(memberRepository.findByIdAndOrganizationIdAndStatus(memberId, orgId, BaseStatusType.ACTIVE))
                .thenReturn(java.util.Optional.of(member));

        boolean result = superAdminService.changeJoinState(orgId, memberId, req);

        assertTrue(result);
        verify(member).changeJoinStatus(JoinStatusType.REJECTED);
        verify(memberGroupRepository, never()).countByIdInAndOrganizationId(anyList(), anyLong());
        verify(orgRedisRepository, never()).saveMemberPermission(anyLong(), anyLong(), anyLong());
    }

    @Test
    @DisplayName("changeJoinState - APPROVED 이고 그룹 유효 → 매핑 및 Redis 동기화")
    void changeJoinState_approved_withGroups_andRedisSync() {
        Long orgId = 1L;
        Long memberId = 10L;

        TransactionSynchronizationManager.initSynchronization();

        ChangeJoinStateRequest req = mock(ChangeJoinStateRequest.class);
        when(req.getStatus()).thenReturn(JoinStatusType.APPROVED.name());
        when(req.getMemberGroupIds()).thenReturn(List.of(1L, 2L));

        Member member = mock(Member.class);
        when(memberRepository.findByIdAndOrganizationIdAndStatus(memberId, orgId, BaseStatusType.ACTIVE))
                .thenReturn(java.util.Optional.of(member));
        when(member.getPermissionCode()).thenReturn(15L);

        when(memberGroupRepository.countByIdInAndOrganizationId(req.getMemberGroupIds(), orgId))
                .thenReturn(2L);

        MemberGroup g1 = mock(MemberGroup.class);
        MemberGroup g2 = mock(MemberGroup.class);
        when(memberGroupRepository.findAllById(req.getMemberGroupIds()))
                .thenReturn(List.of(g1, g2));

        boolean result = superAdminService.changeJoinState(orgId, memberId, req);

        assertTrue(result);
        verify(member).changeJoinStatus(JoinStatusType.APPROVED);
        verify(memberGroupMappingRepository).saveAll(anyList());

        // afterCommit 수동 호출
        for (TransactionSynchronization sync : TransactionSynchronizationManager.getSynchronizations()) {
            sync.afterCommit();
        }

        verify(orgRedisRepository).saveMemberPermission(orgId, memberId, 15L);
    }

    @Test
    @DisplayName("changeJoinState - APPROVED 이지만 그룹 ID가 유효하지 않으면 예외")
    void changeJoinState_approved_invalidGroups() {
        Long orgId = 1L;
        Long memberId = 10L;

        ChangeJoinStateRequest req = mock(ChangeJoinStateRequest.class);
        when(req.getStatus()).thenReturn(JoinStatusType.APPROVED.name());
        when(req.getMemberGroupIds()).thenReturn(List.of(1L, 2L));

        Member member = mock(Member.class);
        when(memberRepository.findByIdAndOrganizationIdAndStatus(memberId, orgId, BaseStatusType.ACTIVE))
                .thenReturn(java.util.Optional.of(member));

        when(memberGroupRepository.countByIdInAndOrganizationId(req.getMemberGroupIds(), orgId))
                .thenReturn(1L); // invalid

        assertThrows(ApiException.class,
                () -> superAdminService.changeJoinState(orgId, memberId, req));

        verify(memberGroupMappingRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("readAllJoinRequest - 가입 요청 + 멤버 그룹 함께 반환")
    void readAllJoinRequest_success() {
        Long orgId = 1L;

        ReadAllJoinRequestItem item = mock(ReadAllJoinRequestItem.class);
        MemberGroupItem groupItem = new MemberGroupItem(1L, "개발팀");

        when(memberRepository.findByOrganizationIdAndJoinStatus(orgId, JoinStatusType.PENDING))
                .thenReturn(List.of(item));
        when(memberGroupRepository.findAllByOrganizationId(orgId))
                .thenReturn(List.of(groupItem));

        ReadAllJoinRequestResponse response = superAdminService.readAllJoinRequest(orgId);

        assertEquals(1, response.getJoinRequests().size());
        assertEquals(1, response.getAllMemberGroups().size());
    }

    @Test
    @DisplayName("withdrawMember - Redis 권한 삭제 후 멤버 비활성화")
    void withdrawMember_success() {
        Long orgId = 1L;
        Long memberId = 10L;

        Member member = mock(Member.class);
        when(memberRepository.findByIdAndOrganizationIdAndStatus(memberId, orgId, BaseStatusType.ACTIVE))
                .thenReturn(java.util.Optional.of(member));
        when(member.getId()).thenReturn(memberId);

        boolean result = superAdminService.withdrawMember(orgId, memberId);

        assertTrue(result);
        verify(orgRedisRepository).deleteMemberPermission(orgId, memberId);
        verify(member).updateToInactive();
    }

    @Test
    @DisplayName("deleteOrganization - 조직 비활성화")
    void deleteOrganization_success() {
        Long orgId = 1L;
        Organization org = mock(Organization.class);
        when(organizationRepository.findById(orgId))
                .thenReturn(java.util.Optional.of(org));

        boolean result = superAdminService.deleteOrganization(orgId);

        assertTrue(result);
        verify(org).updateToInactive();
    }
}