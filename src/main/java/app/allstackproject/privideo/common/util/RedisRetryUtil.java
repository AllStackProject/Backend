package app.allstackproject.privideo.common.util;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

@Slf4j
public class RedisRetryUtil {

    private RedisRetryUtil() {
    }

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_INTERVAL_MS = 50;

    public static <T> T executeWithRetry(Supplier<T> redisCall, String logContext) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                T result = redisCall.get();
                log.debug("Redis 조회 성공");
                return result;
            } catch (Exception e) {
                log.warn("Redis 조회 실패 (시도 {}/{}) - {}", attempt, MAX_RETRIES, logContext);
                if (attempt < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_INTERVAL_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        log.info("Redis 재시도 실패, fallback 필요", logContext);
        return null;
    }
}
