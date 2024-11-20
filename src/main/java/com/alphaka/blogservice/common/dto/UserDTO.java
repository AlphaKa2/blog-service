package com.alphaka.blogservice.common.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * 사용자 정보를 나타내는 DTO
 */
@Getter
@Builder
@ToString
public class UserDTO extends AbstractUser {

    public UserDTO(Long userId, String nickname, String profileImage) {
        super(userId, nickname, profileImage);
    }
}