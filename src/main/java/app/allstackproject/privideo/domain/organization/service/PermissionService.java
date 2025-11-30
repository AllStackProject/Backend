package app.allstackproject.privideo.domain.organization.service;

import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.MEMBER_NOT_FOUND;
import static app.allstackproject.privideo.shared.enums.BaseStatusType.ACTIVE;

import app.allstackproject.privideo.domain.member.entity.Member;
import app.allstackproject.privideo.domain.member.repository.MemberRepository;
import app.allstackproject.privideo.domain.organization.repository.OrgRedisRepository;
import app.allstackproject.privideo.global.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final OrgRedisRepository orgRedisRepository;
    private final MemberRepository memberRepository;


    @Transactional(readOnly = true)
    public long getMemberPermission(Long orgId, Long memberId) {
        Long redisPermission = orgRedisRepository.getMemberPermission(orgId, memberId);

        if (redisPermission != null) {
            log.debug("Redis에서 권한 조회 성공 - memberId: {}, permission: {}",
                    memberId, redisPermission);
            return redisPermission;
        }

        log.info("Redis fallback, DB 조회");

        Member member = memberRepository.findByIdAndOrganizationIdAndStatus(memberId, orgId, ACTIVE)
                .orElseThrow(() -> {
                    return new ApiException(MEMBER_NOT_FOUND);
                });

        long dbPermission = member.getPermissionCode();

        try {
            orgRedisRepository.saveMemberPermission(orgId, memberId, dbPermission);
        } catch (Exception e) {
            log.error("Redis 동기화 실패 - memberId: {}, error: {}",
                    memberId, e.getMessage());
        }

        return dbPermission;
    }
}