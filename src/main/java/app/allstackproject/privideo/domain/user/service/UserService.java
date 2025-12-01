package app.allstackproject.privideo.domain.user.service;

import static app.allstackproject.privideo.shared.enums.BaseStatusType.ACTIVE;
import static app.allstackproject.privideo.shared.enums.BaseStatusType.INACTIVE;
import static app.allstackproject.privideo.domain.organization.dto.enums.JoinStatusType.PENDING;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.ALREADY_LEAVED_USER;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.DB_CONSTRAINT_VIOLATE;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.DUPLICATE_EMAIL;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.INVALID_ORG_CODE;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.INVALID_PASSWORD;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.ORG_CODE_NOT_AVAILABLE;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.PASSWORD_MISMATCH;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.PASSWORD_SAME_AS_CURRENT;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.USER_NOT_FOUND;

import app.allstackproject.privideo.domain.user.dto.enums.GenderType;
import app.allstackproject.privideo.global.exception.ApiException;
import app.allstackproject.privideo.global.security.JwtProvider;
import app.allstackproject.privideo.domain.user.dto.request.PostLoginRequest;
import app.allstackproject.privideo.domain.user.dto.request.PostSignupRequest;
import app.allstackproject.privideo.domain.user.dto.request.UpdateUserInfoRequest;
import app.allstackproject.privideo.domain.user.dto.response.UserInfoResponse;
import app.allstackproject.privideo.domain.member.entity.Member;
import app.allstackproject.privideo.domain.organization.entity.Organization;
import app.allstackproject.privideo.domain.user.entity.User;
import app.allstackproject.privideo.domain.member.repository.MemberRepository;
import app.allstackproject.privideo.domain.organization.repository.OrgRedisRepository;
import app.allstackproject.privideo.domain.organization.repository.OrganizationRepository;
import app.allstackproject.privideo.domain.user.repository.UserRepository;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final OrgRedisRepository orgRedisRepository;

    public boolean signup(@Valid PostSignupRequest postSignupRequest) {
        if (userRepository.existsByEmailAndStatus(postSignupRequest.getEmail(), INACTIVE)) {
            throw new ApiException(ALREADY_LEAVED_USER);
        }

        if (userRepository.existsByEmailAndStatus(postSignupRequest.getEmail(), ACTIVE)) {
            throw new ApiException(DUPLICATE_EMAIL);
        }

        User user = User.create(
                postSignupRequest.getName(),
                postSignupRequest.getEmail(),
                postSignupRequest.getPassword(),
                GenderType.valueOf(postSignupRequest.getGender()),
                postSignupRequest.getPhoneNumber(),
                postSignupRequest.getAge()
        ).hashPassword(passwordEncoder);

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(DB_CONSTRAINT_VIOLATE);
        }

        String orgCode = postSignupRequest.getOrganizationCode();
        if (orgCode != null && !orgCode.isBlank()) {
            Long orgId = orgRedisRepository.getOrgIdByCode(orgCode);
            if (orgId == null) {
                throw new ApiException(ORG_CODE_NOT_AVAILABLE);
            }

            Organization org = organizationRepository.findById(orgId)
                    .orElseThrow(() -> new ApiException(INVALID_ORG_CODE));
            memberRepository.save(Member.create(user, org, postSignupRequest.getName(), false, PENDING));
        }

        return true;
    }

    @Transactional(readOnly = true)
    public String login(@Valid PostLoginRequest postLoginRequest) {
        String email = postLoginRequest.getEmail();

        User user = userRepository.findByEmailAndStatus(email, ACTIVE)
                .orElseThrow(() -> new ApiException(USER_NOT_FOUND));

        if (!user.matchPassword(postLoginRequest.getPassword(), passwordEncoder)) {
            throw new ApiException(INVALID_PASSWORD);
        }

        return jwtProvider.createBootstrapToken(user.getId());
    }

    public UserInfoResponse getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(USER_NOT_FOUND));

        List<Member> members = memberRepository.findByUserIdAndStatus(userId, ACTIVE);
        return UserInfoResponse.of(user, members);
    }

    public boolean updateUserInfo(Long userId, UpdateUserInfoRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(USER_NOT_FOUND));

        user.updateInfo(
                request.getChangedPhoneNum(),
                GenderType.valueOf(request.getChangedGender().toUpperCase()),
                request.getChangedAge()
        );

        if (request.getNewPassword().isEmpty()) {
            return true;
        }

        validateAndUpdatePassword(user, request);
        return true;
    }

    private void validateAndUpdatePassword(User user, UpdateUserInfoRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ApiException(PASSWORD_MISMATCH);
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new ApiException(PASSWORD_SAME_AS_CURRENT);
        }

        user.changePassword(request.getNewPassword(), passwordEncoder);
    }

    public boolean deleteUser(Long userId) {
        memberRepository.inactivateAllByUserId(userId);

        User user = userRepository.findById(userId).orElseThrow(() -> new ApiException(USER_NOT_FOUND));
        user.updateToInactive();

        return true;
    }
}
