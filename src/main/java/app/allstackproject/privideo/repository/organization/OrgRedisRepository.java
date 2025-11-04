package app.allstackproject.privideo.repository.organization;

import app.allstackproject.privideo.common.exception.ApiException;
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

    //orgID로 code 조회
    public String getOrgcodeById(Long orgId) {
        String key = RedisUtil.org(orgId);
        return (String) redisTemplate.opsForHash()
                .get(key, Fields.CODE);
    }

    //orgcode로 ID 조회
    public Long getOrgIdByCode(String code) {
        String key = RedisUtil.orgCode(code);
        String orgId = redisTemplate.opsForValue().get(key);
        return orgId != null ? Long.parseLong(orgId) : null;
    }

    //조직 정보 존재 확인
    public boolean orgExists(Long orgId) {
        if (orgId == null) {
            return false;
        }
        String key = RedisUtil.org(orgId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    //새 조직 코드 발급((최초 생성)
    public void createOrgCode(Long orgId, String code) {
        String orgKey = RedisUtil.org(orgId);
        String codeKey = RedisUtil.orgCode(code);
        long now = System.currentTimeMillis();

        String existingOrgId = redisTemplate.opsForValue().get(codeKey);
        if (existingOrgId != null) {
            throw new ApiException(ORGANIZATION_CODE_IN_USE);
        }

        //조직 HASH 생성
        redisTemplate.opsForHash().put(orgKey, RedisUtil.Fields.CODE, code);
        redisTemplate.opsForHash().put(orgKey, RedisUtil.Fields.UPDATED_AT, String.valueOf(now));

        //코드 역인덱스 생성
        redisTemplate.opsForValue().set(codeKey, String.valueOf(orgId));

        log.debug("조직 코드 생성 - orgId: {}, code: {}", orgId, code);
    }

    //조직 코드 재발급
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

    //조직 탈퇴 시 조직 정보 삭제
    public void deleteOrg(Long orgId) {
        if (orgId == null) {
            return;
        }

        if (!orgExists(orgId)) {
            log.debug("삭제할 조직 없음 - orgId: {}", orgId);
            return;
        }

        String code = getOrgcodeById(orgId);
        String orgKey = RedisUtil.org(orgId);
        redisTemplate.delete(orgKey);

        if (code != null) {
            String codeKey = RedisUtil.orgCode(code);
            redisTemplate.delete(codeKey);
        }

        log.debug("조직 정보 삭제 - orgId: {}, code: {}", orgId, code);
    }
}
