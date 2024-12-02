package com.alphaka.blogservice.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * 사용자 정보를 나타내는 DTO
 */
@Getter
public class UserDTO extends AbstractUser {

    @JsonCreator
    public UserDTO(@JsonProperty("userId") Long userId,
                   @JsonProperty("nickname") String nickname,
                   @JsonProperty("profileImage") String profileImage) {
        super(userId, nickname, profileImage);
    }
}