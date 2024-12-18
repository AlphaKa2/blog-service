package com.alphaka.blogservice.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper defaultObjectMapper() {
        return configureObjectMapper(new ObjectMapper());
    }

    // Redis용 ObjectMapper
    @Bean
    public GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer(ObjectMapper defaultObjectMapper) {
        ObjectMapper redisMapper = configureObjectMapper(defaultObjectMapper.copy());
        // Redis에서는 타입 정보를 포함해야 함
        redisMapper.activateDefaultTyping(
                redisMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        return new GenericJackson2JsonRedisSerializer(redisMapper);
    }

    // 공통 ObjectMapper 설정 메서드
    private ObjectMapper configureObjectMapper(ObjectMapper mapper) {
        // 날짜 처리 모듈 추가
        mapper.registerModule(new JavaTimeModule());
        // JSON 직렬화에서 timestamp 사용 방지
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        // 알 수 없는 프로퍼티 무시
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }
}
