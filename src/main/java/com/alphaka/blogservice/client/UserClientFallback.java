package com.alphaka.blogservice.client;

import com.alphaka.blogservice.common.dto.UserDTO;
import com.alphaka.blogservice.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;

import java.util.List;
import java.util.Set;

/**
 * UserClient의 Fallback 클래스
 * User-Service와 통신이 불가능할 때 호출되어 대체 기능을 수행
 */
@Slf4j
public class UserClientFallback implements UserClient {

    @Override
    public ApiResponse<UserDTO> findUserByNickname(String nickname) {
        log.error("User-Service와 통신에 실패했습니다. nickname: {}", nickname);
        return new ApiResponse<>(HttpStatus.SC_SERVICE_UNAVAILABLE, "사용자 정보를 불러올 수 없습니다.", null);
    }

    @Override
    public ApiResponse<UserDTO> findUserById(Long id) {
        log.error("User-Service와 통신에 실패했습니다. id: {}", id);
        return new ApiResponse<>(HttpStatus.SC_SERVICE_UNAVAILABLE, "사용자 정보를 불러올 수 없습니다.", null);
    }

    @Override
    public ApiResponse<List<UserDTO>> getUsersById(Set<Long> userIds) {
        log.error("User-Service와 통신에 실패했습니다. userIds: {}", userIds);
        return new ApiResponse<>(HttpStatus.SC_SERVICE_UNAVAILABLE, "사용자 정보를 불러올 수 없습니다.", null);
    }
}
