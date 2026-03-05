package app.allstackproject.privideo.domain.video.repository;

import static app.allstackproject.privideo.global.util.RedisUtil.Fields.MEMBER_ID;

import app.allstackproject.privideo.global.util.RedisRetryUtil;
import app.allstackproject.privideo.global.util.RedisUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class VideoRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final long WATCH_SESSION_TTL_HOURS = 2;
    private static final long HOME_CACHE_TTL_MINUTES = 5;
    private static final long VIDEO_INFO_CACHE_TTL_MINUTES = 10;

    public void createWatchSession(String sessionId, Long memberId) {
        String key = RedisUtil.getWatchSessionKey(sessionId);
        String logContext = String.format("시청 세션 저장 = [session:%s,member:%d]", sessionId, memberId);

        RedisRetryUtil.executeVoidWithRetry(
                () -> {
                    Map<String, String> sessionData = new HashMap<>();
                    sessionData.put(MEMBER_ID, String.valueOf(memberId));

                    redisTemplate.opsForHash().putAll(key, sessionData);
                    redisTemplate.expire(key, WATCH_SESSION_TTL_HOURS, TimeUnit.HOURS);

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

    public Long getMemberIdByWatchSession(String sessionId) {
        String key = RedisUtil.getWatchSessionKey(sessionId);
        String logContext = String.format("시청 세션 조회 = [session:%s]", sessionId);

        return RedisRetryUtil.executeWithRetry(
                () -> {
                    Map<Object, Object> rawHash = redisTemplate.opsForHash().entries(key);
                    if (rawHash.isEmpty()) {
                        log.debug("시청 세션 조회 실패 : 세션 없음 [sessionId: {}]", sessionId);
                        return null;
                    }

                    Object memberIdObj = rawHash.get(MEMBER_ID);
                    if (memberIdObj == null) {
                        log.debug("시청 세션 조회 실패 : memberId 없음 [sessionId: {}]", sessionId);
                        return null;
                    }

                    String memberIdStr = memberIdObj.toString();
                    log.debug("시청 세션 조회 성공 [sessionId: {}, memberId: {}]", sessionId, memberIdStr);

                    try {
                        return Long.valueOf(memberIdStr);
                    } catch (NumberFormatException e) {
                        log.warn("시청 세션 memberId 형식 오류 [sessionId: {}, memberId: {}]", sessionId, memberIdStr, e);
                        return null;
                    }
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

    /**
     * 홈 비디오 목록을 캐시에 저장합니다.
     */
    public void cacheHomeVideos(Long orgId, String filter, List<Map<String, Object>> videoData) {
        String key = RedisUtil.getHomeKey(orgId, filter);
        String logContext = String.format("홈 비디오 목록 캐싱 = [org:%d,filter:%s]", orgId, filter);

        RedisRetryUtil.executeVoidWithRetry(
                () -> {
                    try {
                        String json = objectMapper.writeValueAsString(videoData);
                        redisTemplate.opsForValue().set(key, json, HOME_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
                        log.debug("홈 비디오 목록 캐싱 성공 [org: {}, filter: {}]", orgId, filter);
                    } catch (JsonProcessingException e) {
                        log.warn("홈 비디오 목록 JSON 직렬화 실패", e);
                        throw new RuntimeException(e);
                    }
                },
                logContext
        );
    }

    /**
     * 홈 비디오 목록을 캐시에서 조회합니다.
     */
    public List<Map<String, Object>> getCachedHomeVideos(Long orgId, String filter) {
        String key = RedisUtil.getHomeKey(orgId, filter);
        String logContext = String.format("홈 비디오 목록 조회 = [org:%d,filter:%s]", orgId, filter);

        return RedisRetryUtil.executeWithRetry(
                () -> {
                    String json = redisTemplate.opsForValue().get(key);
                    if (json == null) {
                        log.debug("홈 비디오 목록 캐시 미스 [org: {}, filter: {}]", orgId, filter);
                        return null;
                    }

                    try {
                        List<Map<String, Object>> result = objectMapper.readValue(json,
                                new TypeReference<List<Map<String, Object>>>() {});
                        log.debug("홈 비디오 목록 캐시 히트 [org: {}, filter: {}]", orgId, filter);
                        return result;
                    } catch (JsonProcessingException e) {
                        log.warn("홈 비디오 목록 JSON 역직렬화 실패", e);
                        return null;
                    }
                },
                logContext
        );
    }

    /**
     * 비디오 정보를 캐시에 저장합니다.
     */
    public void cacheVideoInfo(Long videoId, Map<String, Object> videoInfo) {
        String key = RedisUtil.getVideoInfoKey(videoId);
        String logContext = String.format("비디오 정보 캐싱 = [video:%d]", videoId);

        RedisRetryUtil.executeVoidWithRetry(
                () -> {
                    try {
                        String json = objectMapper.writeValueAsString(videoInfo);
                        redisTemplate.opsForValue().set(key, json, VIDEO_INFO_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
                        log.debug("비디오 정보 캐싱 성공 [video: {}]", videoId);
                    } catch (JsonProcessingException e) {
                        log.warn("비디오 정보 JSON 직렬화 실패", e);
                        throw new RuntimeException(e);
                    }
                },
                logContext
        );
    }

    /**
     * 비디오 정보를 캐시에서 조회합니다.
     */
    public Map<String, Object> getCachedVideoInfo(Long videoId) {
        String key = RedisUtil.getVideoInfoKey(videoId);
        String logContext = String.format("비디오 정보 조회 = [video:%d]", videoId);

        return RedisRetryUtil.executeWithRetry(
                () -> {
                    String json = redisTemplate.opsForValue().get(key);
                    if (json == null) {
                        log.debug("비디오 정보 캐시 미스 [video: {}]", videoId);
                        return null;
                    }

                    try {
                        Map<String, Object> result = objectMapper.readValue(json,
                                new TypeReference<Map<String, Object>>() {});
                        log.debug("비디오 정보 캐시 히트 [video: {}]", videoId);
                        return result;
                    } catch (JsonProcessingException e) {
                        log.warn("비디오 정보 JSON 역직렬화 실패", e);
                        return null;
                    }
                },
                logContext
        );
    }

    /**
     * 비디오 관련 캐시를 무효화합니다.
     */
    public void invalidateVideoCache(Long videoId) {
        String pattern = RedisUtil.getVideoInfoPattern(videoId);
        String logContext = String.format("비디오 캐시 무효화 = [video:%d]", videoId);

        RedisRetryUtil.executeVoidWithRetry(
                () -> {
                    Set<String> keys = redisTemplate.keys(pattern);
                    if (keys != null && !keys.isEmpty()) {
                        redisTemplate.delete(keys);
                        log.debug("비디오 캐시 무효화 성공 [video: {}, keys: {}]", videoId, keys.size());
                    }
                },
                logContext
        );
    }

    /**
     * 조직의 홈 캐시를 무효화합니다.
     */
    public void invalidateHomeCache(Long orgId) {
        String pattern = RedisUtil.getHomePattern(orgId);
        String logContext = String.format("홈 캐시 무효화 = [org:%d]", orgId);

        RedisRetryUtil.executeVoidWithRetry(
                () -> {
                    Set<String> keys = redisTemplate.keys(pattern);
                    if (keys != null && !keys.isEmpty()) {
                        redisTemplate.delete(keys);
                        log.debug("홈 캐시 무효화 성공 [org: {}, keys: {}]", orgId, keys.size());
                    }
                },
                logContext
        );
    }
}
