package app.allstackproject.privideo.repository.video;

import static app.allstackproject.privideo.common.util.RedisUtil.Fields.MEMBER_ID;

import app.allstackproject.privideo.common.util.RedisRetryUtil;
import app.allstackproject.privideo.common.util.RedisUtil;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class VideoRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public void createWatchSession(String sessionId, Long memberId) {
        String key = RedisUtil.getWatchSessionKey(sessionId);
        String logContext = String.format("시청 세션 저장 = [session:%s,member:%d]", sessionId, memberId);

        RedisRetryUtil.executeVoidWithRetry(
                () -> {
                    Map<String, String> sessionData = new HashMap<>();
                    sessionData.put(MEMBER_ID, String.valueOf(memberId));

                    redisTemplate.opsForHash().putAll(key, sessionData);

                    log.debug("시청 세션 저장 성공 [sessionId: {}, memberId: {}]", sessionId, memberId);
                },
                logContext
        );
    }

    public boolean existsWatchSession(String sessionId) {
        String key = RedisUtil.getWatchSessionKey(sessionId);
        String logContext = String.format("시청 세션 존재 확인 = [session:%s]", sessionId);

        return Boolean.TRUE.equals(
                RedisRetryUtil.executeWithRetry(
                        () -> {
                            boolean exists = Boolean.TRUE.equals(redisTemplate.hasKey(key));
                            log.debug("시청 세션 존재 확인 완료 [sessionId: {}, exists: {}]", sessionId, exists);
                            return exists;
                        },
                        logContext
                )
        );
    }

    public Map<String, String> getWatchSession(String sessionId) {
        String key = RedisUtil.getWatchSessionKey(sessionId);
        String logContext = String.format("시청 세션 조회 = [session:%s]", sessionId);

        return RedisRetryUtil.executeWithRetry(
                () -> {
                    Map<Object, Object> rawHash = redisTemplate.opsForHash().entries(key);
                    if (rawHash.isEmpty()) {
                        log.debug("시청 세션 조회 실패 : 세션 없음 [sessionId: {}]", sessionId);
                        return null;
                    }

                    Map<String, String> sessionData = new HashMap<>();
                    rawHash.forEach((k, v) -> sessionData.put((String) k, (String) v));

                    log.debug("시청 세션 조회 성공 [sessionId: {}, memberId: {}]", sessionId, sessionData.get(MEMBER_ID));
                    return sessionData;
                },
                logContext
        );
    }
    
    public void deleteWatchSession(String sessionId) {
        String key = RedisUtil.getWatchSessionKey(sessionId);
        String logContext = String.format("시청 세션 삭제 = [session:%s]", sessionId);

        RedisRetryUtil.executeVoidWithRetry(
                () -> {
                    redisTemplate.delete(key);
                    log.debug("시청 세션 삭제 성공 [sessionId: {}]", sessionId);
                },
                logContext
        );
    }
}
