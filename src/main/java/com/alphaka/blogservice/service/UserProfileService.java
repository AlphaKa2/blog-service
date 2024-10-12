package com.alphaka.blogservice.service;

import com.alphaka.blogservice.dto.request.UserProfile;
import com.alphaka.blogservice.exception.custom.UnauthorizedException;
import com.alphaka.blogservice.exception.custom.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserProfileService {

    /**
     * HTTP 요청으로부터 사용자 정보 추출
     * @param request HTTP 요청
     * @return UserProfile 현재 사용자 정보
     */
    public UserProfile getUserProfileFromHeader(HttpServletRequest request) {
        log.info("사용자 정보 추출 요청");

        String userIdHeader = request.getHeader("X-USER-ID");
        String userNicknameHeader = request.getHeader("X-USER-NICKNAME");

        if (userIdHeader == null || userNicknameHeader == null) {
            log.error("헤더에서 사용자 정보를 찾을 수 없습니다.");
            throw new UserNotFoundException();
        }

        try {
            Long userId = Long.parseLong(userIdHeader);
            log.info("사용자 정보 추출 완료 - ID: {}, Nickname: {}", userId, userNicknameHeader);
            return new UserProfile(userId, userNicknameHeader);
        } catch (NumberFormatException e) {
            log.error("유효하지 않은 사용자 ID: {}", userIdHeader);
            throw new UnauthorizedException();
        }
    }
}
