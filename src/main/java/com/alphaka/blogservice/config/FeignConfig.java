package com.alphaka.blogservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.Decoder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Configuration
@RequiredArgsConstructor
public class FeignConfig {

    private final ObjectMapper objectMapper;

    @Bean
    public Decoder feignDecoder() {
        ObjectFactory<HttpMessageConverters> messageConverters =
                () -> new HttpMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper));
        return new SpringDecoder(messageConverters);
    }
}
