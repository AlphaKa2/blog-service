package com.alphaka.blogservice.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * 현재 로그인된 사용자의 정보를 담는 DTO
 */
@Getter
@Builder
@ToString
public class CurrentUser extends AbstractUser {
    /** 사용자 역할 (필수) */
    @NotBlank(message = "사용자 역할은 필수 값입니다.")
    private final String role;

    public CurrentUser(Long userId, String nickname, String profileImage, String role) {
        super(userId, nickname, profileImage);
        this.role = role;
    }
}
