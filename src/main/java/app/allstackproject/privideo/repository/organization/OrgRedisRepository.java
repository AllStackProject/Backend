package app.allstackproject.privideo.repository.organization;

import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.common.util.RedisRetryUtil;
import app.allstackproject.privideo.common.util.RedisUtil;
import app.allstackproject.privideo.common.util.RedisUtil.Fields;

import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.ORGANIZATION_CODE_IN_USE;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class OrgRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisScript<String> regenerateOrgCode;

    public boolean saveMemberPermission(Long orgId, Long memberId, long permissionCode) {
        String key = RedisUtil.memberPermission(orgId, memberId);
        String logContext = String.format("saveMemberPermission[org:%d,member:%d,code:%d]",
                orgId, memberId, permissionCode);

        return RedisRetryUtil.executeVoidWithRetry(
                () -> redisTemplate.opsForHash().put(
                        key, Fields.PERMISSION_CODE, String.valueOf(permissionCode)
                ), logContext
        );
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
        return RedisRetryUtil.executeWithRetry(
                () -> (String) redisTemplate.opsForHash().get(key, Fields.CODE),
                String.format("getOrgcodeById[org:%d]", orgId)
        );
    }

    public Map<Long, String> getOrgCodesByIds(List<Long> orgIds) {
        if (orgIds == null || orgIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Object> results = redisTemplate.executePipelined(
                new SessionCallback<Object>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public Object execute(RedisOperations operations) throws DataAccessException {
                        HashOperations<String, String, String> hash =
                                ((RedisOperations<String, String>) operations).opsForHash();

                        for (Long orgId : orgIds) {
                            hash.get(RedisUtil.org(orgId), Fields.CODE);
                        }
                        return null;
                    }
                }
        );

        Map<Long, String> map = new HashMap<>(orgIds.size());
        for (int i = 0; i < orgIds.size(); i++) {
            map.put(orgIds.get(i), (String) results.get(i));
        }
        return map;
    }

    public boolean saveOrgCode(Long orgId, String orgCode) {
        String key = RedisUtil.orgCode(orgCode);
        String logContext = String.format("saveOrgCode[org:%d,code:%s]", orgId, orgCode);

        return RedisRetryUtil.executeVoidWithRetry(
                () -> redisTemplate.opsForValue().set(key, String.valueOf(orgId)),
                logContext
        );
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

    public boolean createOrgCode(Long orgId, String code) {
        String logContext = String.format("createOrgCode[org:%d,code:%s]", orgId, code);

        return Boolean.TRUE.equals(RedisRetryUtil.executeWithRetry(
                () -> {
                    String orgKey = RedisUtil.org(orgId);
                    String codeKey = RedisUtil.orgCode(code);
                    long now = System.currentTimeMillis();

                    String existingOrgId = redisTemplate.opsForValue().get(codeKey);
                    if (existingOrgId != null) {
                        log.warn("조직 코드 이미 사용중 - code: {}", code);
                        return false;
                    }

                    redisTemplate.multi();
                    try {
                        redisTemplate.opsForHash().put(orgKey, Fields.CODE, code);
                        redisTemplate.opsForHash().put(orgKey, Fields.UPDATED_AT, String.valueOf(now));
                        redisTemplate.opsForValue().set(codeKey, String.valueOf(orgId));
                        redisTemplate.exec();

                        log.debug("조직 코드 생성 성공 - orgId: {}, code: {}", orgId, code);
                        return true;
                    } catch (Exception e) {
                        redisTemplate.discard();
                        throw e;
                    }
                },
                logContext
        ));
    }

    public String regenerateCode(Long orgId, String newCode) {
        String logContext = String.format("regenerateCode[org:%d,newCode:%s]", orgId, newCode);

        String result = RedisRetryUtil.executeWithRetry(
                () -> {
                    String orgKey = RedisUtil.org(orgId);
                    String newOrgKey = RedisUtil.orgCode(newCode);
                    long now = System.currentTimeMillis();

                    List<String> keys = Arrays.asList(orgKey, newOrgKey);
                    List<String> args = Arrays.asList(
                            String.valueOf(orgId),
                            newCode,
                            String.valueOf(now)
                    );

                    return redisTemplate.execute(regenerateOrgCode, keys, args.toArray());
                },
                logContext
        );

        if ("CODE_IN_USE".equals(result)) {
            throw new ApiException(ORGANIZATION_CODE_IN_USE);
        }

        log.debug("조직 코드 재발급 완료 - orgId: {}, oldCode: {}, newCode: {}",
                orgId, result, newCode);
        return result;
    }

    public boolean deleteMemberPermission(Long orgId, Long memberId) {
        String key = RedisUtil.memberPermission(orgId, memberId);
        String logContext = String.format("deleteMemberPermission[org:%d,member:%d]",
                orgId, memberId);

        return RedisRetryUtil.executeVoidWithRetry(
                () -> {
                    redisTemplate.delete(key);
                    log.debug("멤버 권한 삭제 - orgId: {}, memberId: {}", orgId, memberId);
                },
                logContext
        );
    }
}
