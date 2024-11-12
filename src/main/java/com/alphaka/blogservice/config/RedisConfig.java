package com.alphaka.blogservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.password}")
    private String password;

    private final GenericJackson2JsonRedisSerializer genericSerializer;

    // Redis 연결을 위한 LettuceConnectionFactory 빈 등록
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(host, port);
        lettuceConnectionFactory.setPassword(password);
        return lettuceConnectionFactory;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

        // Redis 연결
        redisTemplate.setConnectionFactory(redisConnectionFactory());

        // Key 직렬화 설정
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        // Value 직렬화기 설정 (타입 정보를 포함하는 제네릭 직렬화기)
        redisTemplate.setValueSerializer(genericSerializer);

        // Hash Key, Value 직렬화기 설정
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(genericSerializer);

        return redisTemplate;
    }
}
