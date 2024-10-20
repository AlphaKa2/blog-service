package com.alphaka.blogservice.client;

import com.alphaka.blogservice.dto.request.UserInfo;
import com.alphaka.blogservice.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 사용자 정보 조회를 위한 Feign Client
 */
@FeignClient(name = "USER-SERVICE")
public interface UserClient {

    // 닉네임으로 사용자 조회
    @GetMapping("/users/info")
    ApiResponse<UserInfo> findUserByNickname(@RequestParam("nickname") String nickname);

    // ID로 사용자 조회
    @GetMapping("/users/info")
    ApiResponse<UserInfo> findUserById(@RequestParam("id") Long id);
}
