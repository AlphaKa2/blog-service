package com.alphaka.blogservice.common.dto;

import lombok.Getter;

/**
 * 사용자 정보를 나타내는 DTO
 */
@Getter
public class UserDTO extends AbstractUser {

    public UserDTO(Long userId, String nickname, String profileImage) {
        super(userId, nickname, profileImage);
    }
}