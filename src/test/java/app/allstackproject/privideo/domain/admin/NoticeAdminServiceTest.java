package app.allstackproject.privideo.domain.admin;

import static app.allstackproject.privideo.domain.organization.dto.enums.OpenScopeType.GROUP;
import static app.allstackproject.privideo.domain.organization.dto.enums.OpenScopeType.PUBLIC;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.INVALID_MEMBER_GROUP_IDS;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.MEMBER_NOT_IN_ORGANIZATION;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.NOTICE_NOT_IN_ORGANIZATION;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.ORGANIZATION_NOT_FOUND;
import static app.allstackproject.privideo.shared.enums.BaseStatusType.ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import app.allstackproject.privideo.domain.admin.dto.AdminReadNoticeResponse;
import app.allstackproject.privideo.domain.admin.dto.CreateNoticeRequest;
import app.allstackproject.privideo.domain.admin.dto.MemberGroupItem;
import app.allstackproject.privideo.domain.admin.service.NoticeAdminService;
import app.allstackproject.privideo.domain.member.entity.Member;
import app.allstackproject.privideo.domain.member.entity.MemberGroup;
import app.allstackproject.privideo.domain.member.repository.MemberGroupRepository;
import app.allstackproject.privideo.domain.member.repository.MemberRepository;
import app.allstackproject.privideo.domain.notice.entity.Notice;
import app.allstackproject.privideo.domain.notice.entity.NoticeMemberGroupMapping;
import app.allstackproject.privideo.domain.notice.repository.NoticeMemberGroupMappingRepository;
import app.allstackproject.privideo.domain.notice.repository.NoticeRepository;
import app.allstackproject.privideo.domain.organization.entity.Organization;
import app.allstackproject.privideo.domain.organization.repository.OrganizationRepository;
import app.allstackproject.privideo.global.exception.ApiException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class NoticeAdminServiceTest {
    @InjectMocks
    private NoticeAdminService noticeAdminService;

    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private MemberGroupRepository memberGroupRepository;
    @Mock
    private NoticeRepository noticeRepository;
    @Mock
    private NoticeMemberGroupMappingRepository noticeMemberGroupMappingRepository;

    @Test
    @DisplayName("readNotice - 다른 조직 공지는 NOTICE_NOT_IN_ORGANIZATION")
    void readNotice_notInOrg() {
        Long orgId = 1L;
        Long noticeId = 10L;

        given(noticeRepository.findByIdAndOrganizationId(noticeId, orgId))
                .willReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class,
                () -> noticeAdminService.readNotice(orgId, noticeId));

        assertThat(ex.getResponseStatus()).isEqualTo(NOTICE_NOT_IN_ORGANIZATION);
    }

    @Test
    @DisplayName("readNotice - 조회 시 notice.watch() 호출 및 그룹 정보 포함")
    void readNotice_success() {
        Long orgId = 1L;
        Long noticeId = 10L;

        Organization org = mock(Organization.class);
        Notice notice = mock(Notice.class);
        MemberGroup group1 = mock(MemberGroup.class);
        MemberGroup group2 = mock(MemberGroup.class);

        given(noticeRepository.findByIdAndOrganizationId(noticeId, orgId))
                .willReturn(Optional.of(notice));
        given(memberGroupRepository.findAllByOrganizationId(orgId))
                .willReturn(List.of(
                        new MemberGroupItem(1L, "A"),
                        new MemberGroupItem(2L, "B")
                ));

        NoticeMemberGroupMapping m1 = mock(NoticeMemberGroupMapping.class);
        NoticeMemberGroupMapping m2 = mock(NoticeMemberGroupMapping.class);
        given(m1.getMemberGroup()).willReturn(group1);
        given(m2.getMemberGroup()).willReturn(group2);
        given(group1.getId()).willReturn(1L);
        given(group2.getId()).willReturn(2L);

        given(noticeMemberGroupMappingRepository.findAllByNoticeId(noticeId))
                .willReturn(List.of(m1, m2));

        AdminReadNoticeResponse res = noticeAdminService.readNotice(orgId, noticeId);

        verify(notice).watch();
        assertThat(res.getMemberGroups())
                .extracting("id")
                .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    @DisplayName("createNotice - 조직이 없으면 ORGANIZATION_NOT_FOUND")
    void createNotice_orgNotFound() {
        Long orgId = 1L;
        Long memberId = 10L;
        CreateNoticeRequest req = new CreateNoticeRequest("t", "c", PUBLIC, List.of());

        given(organizationRepository.findById(orgId))
                .willReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class,
                () -> noticeAdminService.createNotice(orgId, memberId, req));

        assertThat(ex.getResponseStatus()).isEqualTo(ORGANIZATION_NOT_FOUND);
    }

    @Test
    @DisplayName("createNotice - 조직 멤버가 아니면 MEMBER_NOT_IN_ORGANIZATION")
    void createNotice_memberNotInOrg() {
        Long orgId = 1L;
        Long memberId = 10L;
        CreateNoticeRequest req = new CreateNoticeRequest("t", "c", PUBLIC, List.of());

        Organization org = mock(Organization.class);

        given(organizationRepository.findById(orgId)).willReturn(Optional.of(org));
        given(memberRepository.findByIdAndOrganizationIdAndStatus(memberId, orgId, ACTIVE))
                .willReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class,
                () -> noticeAdminService.createNotice(orgId, memberId, req));

        assertThat(ex.getResponseStatus()).isEqualTo(MEMBER_NOT_IN_ORGANIZATION);
    }

    @Test
    @DisplayName("createNotice - GROUP 공개인데 memberGroups 비어 있으면 INVALID_MEMBER_GROUP_IDS")
    void createNotice_groupScope_emptyGroups() {
        Long orgId = 1L;
        Long memberId = 10L;
        CreateNoticeRequest req = new CreateNoticeRequest("t", "c", GROUP, List.of());

        Organization org = mock(Organization.class);
        Member member = mock(Member.class);

        given(organizationRepository.findById(orgId)).willReturn(Optional.of(org));
        given(memberRepository.findByIdAndOrganizationIdAndStatus(memberId, orgId, ACTIVE))
                .willReturn(Optional.of(member));

        ApiException ex = assertThrows(ApiException.class,
                () -> noticeAdminService.createNotice(orgId, memberId, req));

        assertThat(ex.getResponseStatus()).isEqualTo(INVALID_MEMBER_GROUP_IDS);
    }

    @Test
    @DisplayName("deleteNotice - 다른 조직 공지 삭제 시 NOTICE_NOT_IN_ORGANIZATION")
    void deleteNotice_notInOrg() {
        Long orgId = 1L;
        Long noticeId = 10L;

        given(noticeRepository.findByIdAndOrganizationId(noticeId, orgId))
                .willReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class,
                () -> noticeAdminService.deleteNotice(orgId, noticeId));

        assertThat(ex.getResponseStatus()).isEqualTo(NOTICE_NOT_IN_ORGANIZATION);
    }

    @Test
    @DisplayName("deleteNotice - 정상 삭제 시 매핑 + 공지 삭제")
    void deleteNotice_success() {
        Long orgId = 1L;
        Long noticeId = 10L;
        Notice notice = mock(Notice.class);

        given(noticeRepository.findByIdAndOrganizationId(noticeId, orgId))
                .willReturn(Optional.of(notice));

        boolean result = noticeAdminService.deleteNotice(orgId, noticeId);

        assertThat(result).isTrue();
        verify(noticeMemberGroupMappingRepository).deleteAllByNoticeId(noticeId);
        verify(noticeRepository).delete(notice);
    }
}