package com.alphaka.blogservice.common.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 정보를 나타내는 DTO
 */
@Getter
@NoArgsConstructor
public class UserDTO extends AbstractUser {

    public UserDTO(Long userId, String nickname, String profileImage) {
        super(userId, nickname, profileImage);
    }
}