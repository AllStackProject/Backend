package app.allstackproject.privideo.domain.user;

import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.ALREADY_LEAVED_USER;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.DUPLICATE_EMAIL;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.INVALID_ORG_CODE;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.INVALID_PASSWORD;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.ORG_CODE_NOT_AVAILABLE;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.PASSWORD_MISMATCH;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.PASSWORD_SAME_AS_CURRENT;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.USER_NOT_FOUND;
import static app.allstackproject.privideo.shared.enums.BaseStatusType.ACTIVE;
import static app.allstackproject.privideo.shared.enums.BaseStatusType.INACTIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import app.allstackproject.privideo.domain.member.entity.Member;
import app.allstackproject.privideo.domain.member.repository.MemberRepository;
import app.allstackproject.privideo.domain.organization.entity.Organization;
import app.allstackproject.privideo.domain.organization.repository.OrgRedisRepository;
import app.allstackproject.privideo.domain.organization.repository.OrganizationRepository;
import app.allstackproject.privideo.domain.user.dto.enums.GenderType;
import app.allstackproject.privideo.domain.user.dto.request.PostLoginRequest;
import app.allstackproject.privideo.domain.user.dto.request.PostSignupRequest;
import app.allstackproject.privideo.domain.user.dto.request.UpdateUserInfoRequest;
import app.allstackproject.privideo.domain.user.dto.response.UserInfoResponse;
import app.allstackproject.privideo.domain.user.entity.User;
import app.allstackproject.privideo.domain.user.repository.UserRepository;
import app.allstackproject.privideo.domain.user.service.UserService;
import app.allstackproject.privideo.global.exception.ApiException;
import app.allstackproject.privideo.global.security.JwtProvider;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private OrgRedisRepository orgRedisRepository;

    private static final String EMAIL_ACTIVE = "active@example.com";
    private static final String EMAIL_INACTIVE = "inactive@example.com";
    private static final String PASSWORD = "password1!";
    private static final String ENCODED_PASSWORD = "encodedPw!";
    private static final String ORG_CODE = "ORG123";
    private static final Long ORG_ID = 1L;

    private User activeUser;
    private User inactiveUser;
    private Organization testOrg;

    @BeforeEach
    void setUp() {
        activeUser = User.create(
                "활성 유저",
                EMAIL_ACTIVE,
                ENCODED_PASSWORD,
                GenderType.MALE,
                "01011112222",
                20
        );

        inactiveUser = User.create(
                "비활성 유저",
                EMAIL_INACTIVE,
                ENCODED_PASSWORD,
                GenderType.FEMALE,
                "01033334444",
                30
        );
        inactiveUser.updateToInactive();

        testOrg = Organization.create(
                activeUser,
                "테스트 조직",
                "조직 설명입니다."
        );
    }

    // ===========================
    // signup
    // ===========================

    @Test
    @DisplayName("[signup] 정상 회원가입 - 조직 코드 없음")
    void signup_success_withoutOrg_stateCheck() {
        PostSignupRequest req = buildSignupRequest(EMAIL_ACTIVE, null);

        // 서비스 로직이 INACTIVE -> ACTIVE 순으로 체크한다고 가정하고 둘 다 스텁
        given(userRepository.existsByEmailAndStatus(req.getEmail(), INACTIVE)).willReturn(false);
        given(userRepository.existsByEmailAndStatus(req.getEmail(), ACTIVE)).willReturn(false);

        given(passwordEncoder.encode(PASSWORD)).willReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        boolean result = userService.signup(req);

        assertThat(result).isTrue();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertThat(saved.getName()).isEqualTo(req.getName());
        assertThat(saved.getEmail()).isEqualTo(req.getEmail());
        assertThat(saved.getGender()).isEqualTo(GenderType.valueOf(req.getGender()));
        assertThat(saved.getPhoneNumber()).isEqualTo(req.getPhoneNumber());
        assertThat(saved.getAge()).isEqualTo(req.getAge());
        assertThat(saved.getPassword()).isEqualTo(ENCODED_PASSWORD);
        verifyNoInteractions(orgRedisRepository, organizationRepository, memberRepository);
    }

    @Test
    @DisplayName("[signup] 정상 회원가입 - 조직 코드로 Member 생성")
    void signup_success_withOrg() {
        PostSignupRequest req = buildSignupRequest(EMAIL_ACTIVE, ORG_CODE);

        given(userRepository.existsByEmailAndStatus(req.getEmail(), INACTIVE)).willReturn(false);
        given(userRepository.existsByEmailAndStatus(req.getEmail(), ACTIVE)).willReturn(false);

        given(passwordEncoder.encode(PASSWORD)).willReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        given(orgRedisRepository.getOrgIdByCode(ORG_CODE)).willReturn(ORG_ID);
        given(organizationRepository.findById(ORG_ID)).willReturn(Optional.of(testOrg));

        boolean result = userService.signup(req);

        assertThat(result).isTrue();
        verify(userRepository).save(any(User.class));
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("[signup] INACTIVE 이메일이 존재하면 ALREADY_LEAVED_USER")
    void signup_alreadyLeaved() {
        PostSignupRequest req = buildSignupRequest(EMAIL_INACTIVE, null);

        // 이 케이스에서는 INACTIVE 만 체크하고 바로 예외 던지므로 ACTIVE 스텁 불필요
        given(userRepository.existsByEmailAndStatus(req.getEmail(), INACTIVE))
                .willReturn(true);

        ApiException ex = assertThrows(ApiException.class,
                () -> userService.signup(req));

        assertThat(ex.getResponseStatus()).isEqualTo(ALREADY_LEAVED_USER);
    }

    @Test
    @DisplayName("[signup] ACTIVE 이메일이 존재하면 DUPLICATE_EMAIL")
    void signup_duplicateEmail() {
        PostSignupRequest req = buildSignupRequest(EMAIL_ACTIVE, null);

        given(userRepository.existsByEmailAndStatus(req.getEmail(), INACTIVE)).willReturn(false);
        given(userRepository.existsByEmailAndStatus(req.getEmail(), ACTIVE)).willReturn(true);

        ApiException ex = assertThrows(ApiException.class,
                () -> userService.signup(req));

        assertThat(ex.getResponseStatus()).isEqualTo(DUPLICATE_EMAIL);
    }

    @Test
    @DisplayName("[signup] 조직 코드가 Redis에 없으면 ORG_CODE_NOT_AVAILABLE")
    void signup_orgCodeNotAvailable() {
        PostSignupRequest req = buildSignupRequest(EMAIL_ACTIVE, ORG_CODE);

        given(userRepository.existsByEmailAndStatus(req.getEmail(), INACTIVE)).willReturn(false);
        given(userRepository.existsByEmailAndStatus(req.getEmail(), ACTIVE)).willReturn(false);

        given(passwordEncoder.encode(PASSWORD)).willReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        given(orgRedisRepository.getOrgIdByCode(ORG_CODE)).willReturn(null);

        ApiException ex = assertThrows(ApiException.class,
                () -> userService.signup(req));

        assertThat(ex.getResponseStatus()).isEqualTo(ORG_CODE_NOT_AVAILABLE);
    }

    @Test
    @DisplayName("[signup] Redis에는 있으나 DB에 조직이 없으면 INVALID_ORG_CODE")
    void signup_invalidOrgCode() {
        PostSignupRequest req = buildSignupRequest(EMAIL_ACTIVE, ORG_CODE);

        given(userRepository.existsByEmailAndStatus(req.getEmail(), INACTIVE)).willReturn(false);
        given(userRepository.existsByEmailAndStatus(req.getEmail(), ACTIVE)).willReturn(false);

        given(passwordEncoder.encode(PASSWORD)).willReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        given(orgRedisRepository.getOrgIdByCode(ORG_CODE)).willReturn(ORG_ID);
        given(organizationRepository.findById(ORG_ID)).willReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class,
                () -> userService.signup(req));

        assertThat(ex.getResponseStatus()).isEqualTo(INVALID_ORG_CODE);
    }

    // ===========================
    // login
    // ===========================

    @Test
    @DisplayName("[login] 성공 시 bootstrap 토큰 반환")
    void login_success() {
        PostLoginRequest req = new PostLoginRequest(EMAIL_ACTIVE, PASSWORD);
        User user = mock(User.class);

        given(userRepository.findByEmailAndStatus(req.getEmail(), ACTIVE))
                .willReturn(Optional.of(user));
        given(user.matchPassword(req.getPassword(), passwordEncoder))
                .willReturn(true);
        given(user.getId()).willReturn(1L);
        given(jwtProvider.createBootstrapToken(1L)).willReturn("TOKEN");

        String token = userService.login(req);

        assertThat(token).isEqualTo("TOKEN");
    }

    @Test
    @DisplayName("[login] 회원이 없으면 USER_NOT_FOUND")
    void login_userNotFound() {
        PostLoginRequest req = new PostLoginRequest(EMAIL_ACTIVE, PASSWORD);

        given(userRepository.findByEmailAndStatus(req.getEmail(), ACTIVE))
                .willReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class,
                () -> userService.login(req));

        assertThat(ex.getResponseStatus()).isEqualTo(USER_NOT_FOUND);
    }

    @Test
    @DisplayName("[login] 비밀번호 불일치 시 INVALID_PASSWORD")
    void login_invalidPassword() {
        PostLoginRequest req = new PostLoginRequest(EMAIL_ACTIVE, PASSWORD);
        User user = mock(User.class);

        given(userRepository.findByEmailAndStatus(req.getEmail(), ACTIVE))
                .willReturn(Optional.of(user));
        given(user.matchPassword(req.getPassword(), passwordEncoder))
                .willReturn(false);

        ApiException ex = assertThrows(ApiException.class,
                () -> userService.login(req));

        assertThat(ex.getResponseStatus()).isEqualTo(INVALID_PASSWORD);
    }

    // ===========================
    // getUserInfo
    // ===========================

    @Test
    @DisplayName("[getUserInfo] 정상 조회")
    void getUserInfo_success() {
        Long userId = 1L;

        User user = mock(User.class);
        Member member = mock(Member.class);
        Organization org = mock(Organization.class);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(memberRepository.findByUserIdAndStatus(userId, ACTIVE))
                .willReturn(Collections.singletonList(member));

        // UserInfoResponse.of(...) 안에서 실제로 사용되는 필드들만 스텁
        given(member.getOrganization()).willReturn(org);
        // 어떤 필드는 안 쓸 수도 있으니 lenient 로 처리
        lenient().when(org.getId()).thenReturn(ORG_ID);
        lenient().when(org.getName()).thenReturn("테스트 조직");
        lenient().when(user.getName()).thenReturn("홍길동");
        lenient().when(user.getEmail()).thenReturn(EMAIL_ACTIVE);

        UserInfoResponse response = userService.getUserInfo(userId);

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("[getUserInfo] 유저가 없으면 USER_NOT_FOUND")
    void getUserInfo_userNotFound() {
        Long userId = 1L;

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class,
                () -> userService.getUserInfo(userId));

        assertThat(ex.getResponseStatus()).isEqualTo(USER_NOT_FOUND);
    }

    // ===========================
    // updateUserInfo
    // ===========================

    @Test
    @DisplayName("[updateUserInfo] 유저가 없으면 USER_NOT_FOUND")
    void updateUserInfo_userNotFound() {
        Long userId = 1L;
        UpdateUserInfoRequest req = buildUpdateUserInfoRequest(
                "",
                "",
                GenderType.FEMALE.name()
        );

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class,
                () -> userService.updateUserInfo(userId, req));

        assertThat(ex.getResponseStatus()).isEqualTo(USER_NOT_FOUND);
    }

    @Test
    @DisplayName("[updateUserInfo] 비밀번호 변경 없이 기본 정보만 수정 (실제 엔티티 상태 검증)")
    void updateUserInfo_onlyProfileChange_stateCheck() {
        Long userId = 1L;

        UpdateUserInfoRequest req = buildUpdateUserInfoRequest(
                "",
                "",
                GenderType.FEMALE.name()
        );

        given(userRepository.findById(userId)).willReturn(Optional.of(activeUser));

        boolean result = userService.updateUserInfo(userId, req);

        assertThat(result).isTrue();
        assertThat(activeUser.getPhoneNumber()).isEqualTo("01043214321");
        assertThat(activeUser.getGender()).isEqualTo(GenderType.FEMALE);
        assertThat(activeUser.getAge()).isEqualTo(30);
        assertThat(activeUser.getPassword()).isEqualTo(ENCODED_PASSWORD);
    }

    @Test
    @DisplayName("[updateUserInfo] newPassword / confirmPassword 불일치 시 PASSWORD_MISMATCH")
    void updateUserInfo_passwordMismatch() {
        Long userId = 1L;
        UpdateUserInfoRequest req = buildUpdateUserInfoRequest(
                "changedPw!",
                "confirmPw!!",
                GenderType.FEMALE.name()
        );

        given(userRepository.findById(userId)).willReturn(Optional.of(activeUser));

        ApiException ex = assertThrows(ApiException.class,
                () -> userService.updateUserInfo(userId, req));

        assertThat(ex.getResponseStatus()).isEqualTo(PASSWORD_MISMATCH);
    }

    @Test
    @DisplayName("[updateUserInfo] 현재 비밀번호와 동일하면 PASSWORD_SAME_AS_CURRENT")
    void updateUserInfo_passwordSameAsCurrent() {
        Long userId = 1L;
        String newPassword = "samePw!1";
        UpdateUserInfoRequest req = buildUpdateUserInfoRequest(
                newPassword,
                newPassword,
                GenderType.FEMALE.name()
        );
        User user = mock(User.class);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(user.getPassword()).willReturn(ENCODED_PASSWORD);
        given(passwordEncoder.matches(newPassword, ENCODED_PASSWORD)).willReturn(true);

        ApiException ex = assertThrows(ApiException.class,
                () -> userService.updateUserInfo(userId, req));

        assertThat(ex.getResponseStatus()).isEqualTo(PASSWORD_SAME_AS_CURRENT);
    }

    @Test
    @DisplayName("[updateUserInfo] 비밀번호 정상 변경 (실제 엔티티 상태 검증)")
    void updateUserInfo_changePassword_stateCheck() {
        Long userId = 1L;
        String newPassword = "newPw!1";
        String encodedNewPw = "encodedNewPw!";

        User user = User.create(
                "홍길동",
                EMAIL_ACTIVE,
                ENCODED_PASSWORD,
                GenderType.MALE,
                "01011112222",
                20
        );

        UpdateUserInfoRequest req = buildUpdateUserInfoRequest(
                newPassword,
                newPassword,
                GenderType.FEMALE.name()
        );

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(newPassword, ENCODED_PASSWORD)).willReturn(false);
        given(passwordEncoder.encode(newPassword)).willReturn(encodedNewPw);

        boolean result = userService.updateUserInfo(userId, req);

        assertThat(result).isTrue();
        assertThat(user.getPhoneNumber()).isEqualTo("01043214321");
        assertThat(user.getGender()).isEqualTo(GenderType.FEMALE);
        assertThat(user.getAge()).isEqualTo(30);
        assertThat(user.getPassword()).isEqualTo(encodedNewPw);
    }

    // ===========================
    // deleteUser
    // ===========================

    @Test
    @DisplayName("[deleteUser] 유저가 없으면 USER_NOT_FOUND")
    void deleteUser_notFound() {
        Long userId = 1L;

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class,
                () -> userService.deleteUser(userId));

        assertThat(ex.getResponseStatus()).isEqualTo(USER_NOT_FOUND);
    }

    @Test
    @DisplayName("[deleteUser] 정상 시 memberRepository.inactivateAllByUserId 호출 및 유저 INACTIVE 처리")
    void deleteUser_success() {
        Long userId = 1L;
        User user = mock(User.class);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        boolean result = userService.deleteUser(userId);

        assertThat(result).isTrue();
        verify(memberRepository).inactivateAllByUserId(userId);
        verify(user).updateToInactive();
    }

    // ===========================
    // helper methods
    // ===========================

    private PostSignupRequest buildSignupRequest(String email, String orgCode) {
        return new PostSignupRequest(
                "홍길동",
                email,
                PASSWORD,
                GenderType.MALE.name(),
                20,
                "01012341234",
                orgCode
        );
    }

    private UpdateUserInfoRequest buildUpdateUserInfoRequest(
            String newPassword,
            String confirmPassword,
            String changedGender
    ) {
        return new UpdateUserInfoRequest(
                newPassword,
                30,
                confirmPassword,
                changedGender,
                "01043214321"
        );
    }
}