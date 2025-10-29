package app.allstackproject.privideo.service.user;

import static app.allstackproject.privideo.common.enumStatus.JoinStatusType.PENDING;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.DB_CONSTRAINT_VIOLATE;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.DUPLICATE_EMAIL;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.INVALID_ORG_CODE;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.INVALID_PASSWORD;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.USER_NOT_FOUND;

import app.allstackproject.privideo.common.enumStatus.GenderType;
import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.common.jwt.JwtProvider;
import app.allstackproject.privideo.dto.user.PatchPasswordRequest;
import app.allstackproject.privideo.dto.user.PostLoginRequest;
import app.allstackproject.privideo.dto.user.PostSignupRequest;
import app.allstackproject.privideo.entity.Member;
import app.allstackproject.privideo.entity.Organization;
import app.allstackproject.privideo.entity.User;
import app.allstackproject.privideo.repository.member.MemberRepository;
import app.allstackproject.privideo.repository.organization.OrganizationRepository;
import app.allstackproject.privideo.repository.user.UserRepository;
import jakarta.validation.Valid;
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

    public boolean signup(@Valid PostSignupRequest postSignupRequest) {
        if (userRepository.existsByEmail(postSignupRequest.getEmail())) {
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
            Organization org = organizationRepository.findByCode(orgCode.trim())
                    .orElseThrow(() -> new ApiException(INVALID_ORG_CODE));
            memberRepository.save(Member.create(user, org, false, PENDING));
        }

        return true;
    }

    @Transactional(readOnly = true)
    public String login(@Valid PostLoginRequest postLoginRequest) {
        String email = postLoginRequest.getEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(USER_NOT_FOUND));

        if (!user.matchPassword(postLoginRequest.getPassword(), passwordEncoder)) {
            throw new ApiException(INVALID_PASSWORD);
        }

        return jwtProvider.createBootstrapToken(user.getId());
    }

    public boolean patchPassword(Long userId, @Valid PatchPasswordRequest patchPasswordRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(USER_NOT_FOUND));

        if (!passwordEncoder.matches(patchPasswordRequest.getCurrentPassword(), user.getPassword())) {
            throw new ApiException(INVALID_PASSWORD);
        }

        user.changePassword(patchPasswordRequest.getNewPassword(), passwordEncoder);
        return true;
    }
}
