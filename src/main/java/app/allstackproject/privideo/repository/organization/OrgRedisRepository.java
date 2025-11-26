package app.allstackproject.privideo.repository.organization;

import app.allstackproject.privideo.common.exception.ApiException;
import app.allstackproject.privideo.common.util.RedisRetryUtil;
import app.allstackproject.privideo.common.util.RedisUtil;

import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.ORGANIZATION_CODE_IN_USE;
import static app.allstackproject.privideo.common.util.RedisUtil.Fields.*;

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

    public void saveMemberPermission(Long orgId, Long memberId, long permissionCode) {
        String key = RedisUtil.getMemberPermissionKey(orgId, memberId);
        String logContext = String.format("멤버 권한 캐싱 = [org:%d,member:%d,permissionCode:%d]", orgId, memberId,
                permissionCode);

        RedisRetryUtil.executeVoidWithRetry(
                () -> {
                    redisTemplate.opsForHash().put(
                            key, PERMISSION_CODE, String.valueOf(permissionCode)
                    );
                    log.debug("멤버 권한 캐싱 성공 [org: {},member: {},permissionCode: {}]", orgId, memberId, permissionCode);
                },
                logContext
        );
    }

    public Long getMemberPermission(Long orgId, Long memberId) {
        String key = RedisUtil.getMemberPermissionKey(orgId, memberId);
        String logContext = String.format("멤버 권한 조회 = [org:%d,member:%d]", orgId, memberId);

        return RedisRetryUtil.executeWithRetry(
                () -> {
                    String permissionCode = (String) redisTemplate.opsForHash().get(key, PERMISSION_CODE);
                    log.debug("멤버 권한 조회 성공 [org: {},member: {},permissionCode: {}]", orgId, memberId, permissionCode);

                    return permissionCode != null ? Long.parseLong(permissionCode) : null;
                },
                logContext
        );
    }

    public void deleteMemberPermission(Long orgId, Long memberId) {
        String key = RedisUtil.getMemberPermissionKey(orgId, memberId);
        String logContext = String.format("멤버 권한 삭제 = [org:%d,member:%d]", orgId, memberId);

        RedisRetryUtil.executeVoidWithRetry(
                () -> {
                    redisTemplate.delete(key);
                    log.debug("멤버 권한 삭제 성공 [orgId: {}, memberId: {}]", orgId, memberId);
                },
                logContext
        );
    }

    public String getOrgCodeById(Long orgId) {
        String key = RedisUtil.getOrgKey(orgId);
        String logContext = String.format("조직 아이디로 조직 코드 조회 = [org:%d]", orgId);

        return RedisRetryUtil.executeWithRetry(
                () -> {
                    String orgCode = (String) redisTemplate.opsForHash().get(key, CODE);
                    log.debug("조직 아이디로 조직 코드 조회 성공 [org: {},orgCode: {}]", orgId, orgCode);

                    return orgCode;
                },
                logContext
        );
    }

    public Map<Long, String> getOrgCodesByIds(List<Long> orgIds) {
        if (orgIds == null || orgIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String logContext = String.format("조직 코드로 여러 조직 코드 조회 = [count:%d]", orgIds.size());

        return RedisRetryUtil.executeWithRetry(
                () -> {
                    List<Object> results = redisTemplate.executePipelined(
                            new SessionCallback<Object>() {
                                @Override
                                @SuppressWarnings("unchecked")
                                public Object execute(RedisOperations operations) throws DataAccessException {
                                    HashOperations<String, String, String> hash = ((RedisOperations<String, String>) operations).opsForHash();

                                    for (Long orgId : orgIds) {
                                        hash.get(RedisUtil.getOrgKey(orgId), CODE);
                                    }
                                    return null;
                                }
                            }
                    );

                    Map<Long, String> orgCodeMap = new HashMap<>(orgIds.size());
                    for (int i = 0; i < orgIds.size(); i++) {
                        orgCodeMap.put(orgIds.get(i), (String) results.get(i));
                    }
                    return orgCodeMap;
                },
                logContext
        );
    }

    public Long getOrgIdByCode(String orgCode) {
        String key = RedisUtil.getOrgCodeKey(orgCode);
        String logContext = String.format("조직 코드로 조직 아이디 조회 = [orgCode:%s]", orgCode);

        return RedisRetryUtil.executeWithRetry(
                () -> {
                    String orgId = redisTemplate.opsForValue().get(key);
                    log.debug("조직 코드로 조직 아이디 조회 성공 [org: {},orgCode: {}]", orgId, orgCode);

                    return orgId != null ? Long.parseLong(orgId) : null;
                },
                logContext
        );
    }

    public void createOrgCode(Long orgId, String orgCode) {
        String logContext = String.format("조직 코드 생성 = [org:%d,orgCode:%s]", orgId, orgCode);

        RedisRetryUtil.executeWithRetry(
                () -> {
                    String orgKey = RedisUtil.getOrgKey(orgId);
                    String codeKey = RedisUtil.getOrgCodeKey(orgCode);
                    long now = System.currentTimeMillis();

                    String existingCode = (String) redisTemplate.opsForHash().get(orgKey, CODE);
                    if (existingCode != null) {
                        log.warn("조직 코드 생성 실패 : 조직에 이미 코드 존재 [orgId: {}, existingCode: {}]", orgId, existingCode);
                        return false;
                    }

                    String existingOrgId = redisTemplate.opsForValue().get(codeKey);
                    if (existingOrgId != null) {
                        log.warn("조직 코드 생성 실패 : 이미 사용 중인 코드 [orgCode: {}]", orgCode);
                        return false;
                    }

                    redisTemplate.multi();
                    try {
                        redisTemplate.opsForHash().put(orgKey, CODE, orgCode);
                        redisTemplate.opsForHash().put(orgKey, UPDATED_AT, String.valueOf(now));
                        redisTemplate.opsForValue().set(codeKey, String.valueOf(orgId));
                        redisTemplate.exec();

                        log.debug("조직 코드 생성 성공 [orgId: {}, orgCode: {}]", orgId, orgCode);
                        return true;
                    } catch (Exception e) {
                        redisTemplate.discard();
                        throw e;
                    }
                },
                logContext
        );
    }

    public void regenerateCode(Long orgId, String newCode) {
        String logContext = String.format("조직 코드 재생성 = [org:%d,orgCode:%s]", orgId, newCode);

        String result = RedisRetryUtil.executeWithRetry(
                () -> {
                    String orgKey = RedisUtil.getOrgKey(orgId);
                    String newOrgKey = RedisUtil.getOrgCodeKey(newCode);
                    long now = System.currentTimeMillis();

                    List<String> keys = Arrays.asList(orgKey, newOrgKey);
                    List<String> args = Arrays.asList(
                            String.valueOf(orgId),
                            newCode,
                            String.valueOf(now)
                    );

                    log.debug("조직 코드 재발급 성공 [orgId: {}, orgCode: {}]", orgId, newCode);
                    return redisTemplate.execute(regenerateOrgCode, keys, args.toArray());
                },
                logContext
        );

        if ("CODE_IN_USE".equals(result)) {
            throw new ApiException(ORGANIZATION_CODE_IN_USE);
        }

    }
}
