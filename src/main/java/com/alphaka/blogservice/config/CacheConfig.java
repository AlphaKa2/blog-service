package com.alphaka.blogservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
@RequiredArgsConstructor
public class CacheConfig {

    private final GenericJackson2JsonRedisSerializer genericSerializer;

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // 기본 캐시 설정
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(genericSerializer))
                .entryTtl(Duration.ofMinutes(30)) // 기본 캐시 만료 시간
                .disableCachingNullValues(); // null 값 캐싱 비활성화

        // 특정 캐시에 대한 설정
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // 게시글 상세 조회 캐시 설정
        cacheConfigurations.put("blogService:postDetails", RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(genericSerializer))
                .entryTtl(Duration.ofMinutes(30)) // 캐시 만료 시간
                .disableCachingNullValues());

        // 게시글 목록 조회 캐시 설정
        cacheConfigurations.put("blogService:postList", RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(genericSerializer))
                .entryTtl(Duration.ofMinutes(30)) // 캐시 만료 시간
                .disableCachingNullValues());

        // 태그 목록 조회 캐시 설정
        cacheConfigurations.put("blogService:tagList", RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(genericSerializer))
                .entryTtl(Duration.ofMinutes(30)) // 캐시 만료 시간
                .disableCachingNullValues());

        // 댓글 조회 캐시 설정
        cacheConfigurations.put("blogService:comments", RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(genericSerializer))
                .entryTtl(Duration.ofMinutes(30)) // 캐시 만료 시간
                .disableCachingNullValues());

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
