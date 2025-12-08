package app.allstackproject.privideo.domain.organization;

import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.ALREADY_LEAVED_MEMBER;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.ORG_CODE_NOT_AVAILABLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import app.allstackproject.privideo.domain.member.entity.Member;
import app.allstackproject.privideo.domain.member.repository.MemberGroupRepository;
import app.allstackproject.privideo.domain.member.repository.MemberRepository;
import app.allstackproject.privideo.domain.organization.entity.Organization;
import app.allstackproject.privideo.domain.organization.repository.OrgRedisRepository;
import app.allstackproject.privideo.domain.organization.repository.OrganizationRepository;
import app.allstackproject.privideo.domain.organization.service.OrganizationService;
import app.allstackproject.privideo.domain.organization.service.PermissionService;
import app.allstackproject.privideo.domain.user.entity.User;
import app.allstackproject.privideo.domain.user.repository.UserRepository;
import app.allstackproject.privideo.domain.video.repository.CategoryRepository;
import app.allstackproject.privideo.global.exception.ApiException;
import app.allstackproject.privideo.global.security.JwtProvider;
import app.allstackproject.privideo.global.util.CdnUrlProvider;
import app.allstackproject.privideo.global.util.S3Util;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @InjectMocks
    private OrganizationService organizationService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private MemberGroupRepository memberGroupRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private OrgRedisRepository orgRedisRepository;
    @Mock
    private PermissionService permissionService;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private CdnUrlProvider cdnUrlProvider;
    @Mock
    private S3Util s3Util;

    @Test
    @DisplayName("조직 코드가 Redis에 없으면 ORG_CODE_NOT_AVAILABLE 예외")
    void joinOrg_orgCodeNotFound() {
        // given
        Long userId = 1L;
        String orgCode = "ABC123";
        String nickname = "닉네임";

        User user = Mockito.mock(User.class);

        given(userRepository.findById(userId))
                .willReturn(Optional.of(user));
        given(orgRedisRepository.getOrgIdByCode(orgCode))
                .willReturn(null); // orgId 없음

        // when
        ApiException ex = assertThrows(ApiException.class,
                () -> organizationService.joinOrg(userId, orgCode, nickname));

        // then
        assertThat(ex.getResponseStatus()).isEqualTo(ORG_CODE_NOT_AVAILABLE);
    }

    @Test
    @DisplayName("INACTIVE 상태의 멤버가 존재하면 ALREADY_LEAVED_MEMBER 예외")
    void joinOrg_alreadyLeavedMember() {
        // given
        Long userId = 1L;
        String orgCode = "ABC123";
        Long orgId = 10L;

        User user = Mockito.mock(User.class);
        Organization org = Mockito.mock(Organization.class);
        Member inactiveMember = Mockito.mock(Member.class);

        given(userRepository.findById(userId))
                .willReturn(Optional.of(user));
        given(orgRedisRepository.getOrgIdByCode(orgCode))
                .willReturn(orgId);
        given(organizationRepository.findById(orgId))
                .willReturn(Optional.of(org));
        // INACTIVE 멤버 존재
        given(memberRepository.findByUserIdAndOrganizationIdAndStatus(
                ArgumentMatchers.eq(userId),
                ArgumentMatchers.eq(orgId),
                ArgumentMatchers.eq(app.allstackproject.privideo.shared.enums.BaseStatusType.INACTIVE)
        )).willReturn(Optional.of(inactiveMember));

        // when
        ApiException ex = assertThrows(ApiException.class,
                () -> organizationService.joinOrg(userId, orgCode, "닉네임"));

        // then
        assertThat(ex.getResponseStatus()).isEqualTo(ALREADY_LEAVED_MEMBER);
    }
}
