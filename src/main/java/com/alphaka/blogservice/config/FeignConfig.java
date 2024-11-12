package com.alphaka.blogservice.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.codec.Decoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Configuration
public class FeignConfig {

    @Bean
    public Decoder feignDecoder(ObjectMapper objectMapper) {
        // ObjectMapper 설정
        ObjectMapper mapper = objectMapper.copy();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // SpringDecoder를 사용하여 Jackson2JsonDecoder 설정
        ObjectFactory<HttpMessageConverters> messageConverters = () -> new HttpMessageConverters(new MappingJackson2HttpMessageConverter(mapper));
        return new SpringDecoder(messageConverters);
    }
}
