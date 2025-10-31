package app.allstackproject.privideo.repository.organization;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrgRedisRepository {

    public final RedisTemplate<String, String> redisTemplate;

    
}
