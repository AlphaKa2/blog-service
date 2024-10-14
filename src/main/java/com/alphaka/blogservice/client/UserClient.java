package com.alphaka.blogservice.client;

import com.alphaka.blogservice.dto.request.UserInfo;
import com.alphaka.blogservice.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 사용자 정보 조회를 위한 Feign Client
 */
@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/users/info/{userId}")
    ApiResponse<UserInfo> findUser(@PathVariable("userId") Long userId);

    @GetMapping("/users/info/nickname/{nickname}")
    ApiResponse<UserInfo> findUser(@PathVariable("nickname") String nickname);
}
