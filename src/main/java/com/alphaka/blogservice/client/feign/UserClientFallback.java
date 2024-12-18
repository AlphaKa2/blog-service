package com.alphaka.blogservice.client.feign;

import com.alphaka.blogservice.common.dto.UserDTO;
import com.alphaka.blogservice.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.slf4j.MDC;

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
        log.error("User-Service 통신 실패 | nickname: {} | traceId: {}", nickname, MDC.get("traceId"));
        return new ApiResponse<>(
                HttpStatus.SC_SERVICE_UNAVAILABLE,
                "사용자 정보를 불러올 수 없습니다. 잠시 후 다시 시도해주세요",
                null
        );
    }

    @Override
    public ApiResponse<UserDTO> findUserById(Long id) {
        log.error("User-Service 통신 실패 | id: {} | traceId: {}", id, MDC.get("traceId"));
        return new ApiResponse<>(
                HttpStatus.SC_SERVICE_UNAVAILABLE,
                "사용자 정보를 불러올 수 없습니다. 잠시 후 다시 시도해주세요",
                null
        );
    }

    @Override
    public ApiResponse<List<UserDTO>> getUsersById(Set<Long> userIds) {
        log.error("User-Service 통신 실패 | userIds: {} | traceId: {}", userIds, MDC.get("traceId"));
        return new ApiResponse<>(
                HttpStatus.SC_SERVICE_UNAVAILABLE,
                "사용자 정보를 불러올 수 없습니다. 잠시 후 다시 시도해주세요",
                null
        );
    }
}
