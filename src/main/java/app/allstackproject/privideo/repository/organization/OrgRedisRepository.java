package app.allstackproject.privideo.repository.organization;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrgRedisRepository {

    public final RedisTemplate<String, String> redisTemplate;

//    public void setOrganization(Long orgId, String code) {
//        redisTemplate.opsForHash().putAll(
//                "org:" + orgId,
//                Map.of("code", code, "updatedAt", String.valueOf())
//        );
//    }
}
