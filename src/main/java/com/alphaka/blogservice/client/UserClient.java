package com.alphaka.blogservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 사용자 정보 조회를 위한 Feign Client
 */
@FeignClient(name = "user-service")
public interface UserClient {

    // 특정 사용자 존재 여부 확인
    @GetMapping("/api/users/exist/{userId}")
    @ResponseStatus(HttpStatus.OK)
    boolean isUserExists(@PathVariable("userId") Long userId);
}
