package app.allstackproject.privideo.repository.organization;

import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.common.util.RedisRetryUtil;
import app.allstackproject.privideo.common.util.RedisUtil;
import app.allstackproject.privideo.common.util.RedisUtil.Fields;

import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.ORGANIZATION_CODE_IN_USE;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class OrgRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisScript<String> regenerateOrgCode;

    public void saveMemberPermission(Long orgId, Long memberId, long permissionCode) {
        String key = RedisUtil.memberPermission(orgId, memberId);
        redisTemplate.opsForHash().put(key, Fields.PERMISSION_CODE, String.valueOf(permissionCode));
    }

    public Long getMemberPermission(Long orgId, Long memberId) {
        String key = RedisUtil.memberPermission(orgId, memberId);
        return RedisRetryUtil.executeWithRetry(
                () -> {
                    String permission = (String) redisTemplate.opsForHash().get(key, Fields.PERMISSION_CODE);
                    return permission != null ? Long.parseLong(permission) : null;
                },
                "memberPermission - memberId: " + memberId
        );
    }


    public String getOrgcodeById(Long orgId) {
        String key = RedisUtil.org(orgId);
        return (String) redisTemplate.opsForHash()
                .get(key, Fields.CODE);
    }

    public void saveOrgCode(Long orgId, String orgCode) {
        String key = RedisUtil.orgCode(orgCode);
        redisTemplate.opsForValue().set(key, String.valueOf(orgId));
    }

    public Long getOrgIdByCode(String orgCode) {
        String key = RedisUtil.orgCode(orgCode);

        return RedisRetryUtil.executeWithRetry(
                () -> {
                    String orgId = redisTemplate.opsForValue().get(key);
                    return orgId != null ? Long.parseLong(orgId) : null;
                },
                "getOrgIdByCode - orgCode: " + orgCode
        );
    }

    public void createOrgCode(Long orgId, String code) {
        String orgKey = RedisUtil.org(orgId);
        String codeKey = RedisUtil.orgCode(code);
        long now = System.currentTimeMillis();

        String existingOrgId = redisTemplate.opsForValue().get(codeKey);
        if (existingOrgId != null) {
            throw new ApiException(ORGANIZATION_CODE_IN_USE);
        }

        redisTemplate.opsForHash().put(orgKey, RedisUtil.Fields.CODE, code);
        redisTemplate.opsForHash().put(orgKey, RedisUtil.Fields.UPDATED_AT, String.valueOf(now));

        redisTemplate.opsForValue().set(codeKey, String.valueOf(orgId));

        log.debug("조직 코드 생성 - orgId: {}, code: {}", orgId, code);
    }

    public String regenerateCode(Long orgId, String newCode) {
        String orgKey = RedisUtil.org(orgId);
        String newOrgKey = RedisUtil.orgCode(newCode);
        long now = System.currentTimeMillis();

        List<String> keys = Arrays.asList(orgKey, newOrgKey);
        List<String> args = Arrays.asList(
                String.valueOf(orgId),
                newCode,
                String.valueOf(now)
        );

        String result = redisTemplate.execute(regenerateOrgCode, keys, args.toArray());

        if ("CODE_IN_USE".equals(result)) {
            throw new ApiException(ORGANIZATION_CODE_IN_USE);
        }

        log.debug("조직 코드 재발급 - orgId: {}, oldCode: {}, newCode: {}", orgId, result, newCode);

        return result;
    }

    public void deleteMemberPermission(Long orgId, Long memberId) {
        String key = RedisUtil.memberPermission(orgId, memberId);
        redisTemplate.delete(key);
        log.debug("멤버 권한 삭제 - orgId: {}, memberId: {}", orgId, memberId);
    }
}
