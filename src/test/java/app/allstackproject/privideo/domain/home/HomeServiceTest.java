package app.allstackproject.privideo.domain.home;

import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.MEMBER_NOT_IN_ORGANIZATION;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.ORGANIZATION_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import app.allstackproject.privideo.domain.home.service.HomeService;
import app.allstackproject.privideo.domain.member.repository.MemberGroupMappingRepository;
import app.allstackproject.privideo.domain.member.repository.MemberRepository;
import app.allstackproject.privideo.domain.notice.repository.NoticeMemberGroupMappingRepository;
import app.allstackproject.privideo.domain.notice.repository.NoticeRepository;
import app.allstackproject.privideo.domain.organization.entity.Organization;
import app.allstackproject.privideo.domain.organization.repository.OrganizationRepository;
import app.allstackproject.privideo.domain.video.repository.VideoRepository;
import app.allstackproject.privideo.global.exception.ApiException;
import app.allstackproject.privideo.global.util.CdnUrlProvider;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HomeServiceTest {

    @InjectMocks
    private HomeService homeService;

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private VideoRepository videoRepository;
    @Mock
    private NoticeRepository noticeRepository;
    @Mock
    private NoticeMemberGroupMappingRepository noticeMemberGroupMappingRepository;
    @Mock
    private MemberGroupMappingRepository memberGroupMappingRepository;
    @Mock
    private CdnUrlProvider cdnUrlProvider;

    @Test
    @DisplayName("조직이 없으면 ORGANIZATION_NOT_FOUND 예외")
    void readHome_orgNotFound() {
        Long memberId = 1L;
        Long orgId = 10L;

        given(organizationRepository.findById(orgId))
                .willReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class,
                () -> homeService.readHome(memberId, orgId, "ALL"));

        assertThat(ex.getResponseStatus()).isEqualTo(ORGANIZATION_NOT_FOUND);
    }

    @Test
    @DisplayName("멤버가 조직에 속해있지 않으면 MEMBER_NOT_IN_ORGANIZATION 예외")
    void readHome_memberNotInOrg() {
        Long memberId = 1L;
        Long orgId = 10L;

        Organization org = Mockito.mock(Organization.class);

        given(organizationRepository.findById(orgId))
                .willReturn(Optional.of(org));
        given(memberRepository.findByIdAndOrganizationIdAndStatus(
                memberId, orgId, app.allstackproject.privideo.shared.enums.BaseStatusType.ACTIVE))
                .willReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class,
                () -> homeService.readHome(memberId, orgId, "ALL"));

        assertThat(ex.getResponseStatus()).isEqualTo(MEMBER_NOT_IN_ORGANIZATION);
    }

    // 정상 케이스는 DTO 매핑이 길어서, 필요하면 나중에 given/thenReturn으로 다 채우는 패턴으로 추가하면 됨
}
