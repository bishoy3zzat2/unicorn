package com.loyalixa.backend.config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
@Configuration
public class RedisConfig {
    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        try {
            RedisConnectionFactory factory = template.getConnectionFactory();
            if (factory != null) {
                factory.getConnection().ping();
                logger.info("Redis connection established successfully");
            }
        } catch (Exception e) {
            logger.warn("Redis is not available. Token blacklisting will be disabled. Error: {}", e.getMessage());
            logger.warn(
                    "To enable Redis, start it using: docker-compose up -d redis-cache (from the 'others' directory)");
        }
        return template;
    }
}