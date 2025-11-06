package app.allstackproject.privideo.service.permission;

import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.entity.Member;
import app.allstackproject.privideo.repository.member.MemberRepository;
import app.allstackproject.privideo.repository.organization.OrgRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.MEMBER_NOT_FOUND;

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

        Member member = memberRepository.findByIdAndOrganizationId(orgId, memberId)
                .orElseThrow(() -> {
                    return new ApiException(MEMBER_NOT_FOUND);
                });

        long dbPermission = member.getPermissionCode();

        try {
            orgRedisRepository.saveMemberPermission(memberId, orgId, dbPermission);
        } catch (Exception e) {
            log.error("Redis 동기화 실패 - memberId: {}, error: {}",
                    memberId, e.getMessage());
        }

        return dbPermission;
    }
}