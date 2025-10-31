package app.allstackproject.privideo.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@RequiredArgsConstructor
@EnableRedisRepositories
public class RedisConfig {

    private final RedisProperties redisProperties;

    // lettuce
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        LettuceConnectionFactory factory = new LettuceConnectionFactory(
                redisProperties.getHost(),
                redisProperties.getPort()
        );

        if (redisProperties.getPassword() != null && !redisProperties.getPassword().isEmpty()) {
            factory.setPassword(redisProperties.getPassword());
        }

        //db 인덱스 기본 값 0
        factory.setDatabase(redisProperties.getDatabase());

        return factory;
    }

    // Redis template
    @Bean
    public RedisTemplate<String, String> redisTemplate() {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());   //connection
        redisTemplate.setKeySerializer(new StringRedisSerializer());    // key
        redisTemplate.setValueSerializer(new StringRedisSerializer());  // value

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
