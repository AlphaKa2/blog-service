package com.alphaka.blogservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "auth-service")
public interface AuthClient {

    // 사용자 정보 추출
    @GetMapping("/auth/extract/{userId}")
    Long extractUserId(String userId);
}