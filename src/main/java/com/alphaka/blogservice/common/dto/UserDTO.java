package com.alphaka.blogservice.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 정보를 나타내는 DTO
 */
@Getter
@NoArgsConstructor
public class UserDTO extends AbstractUser {

    @JsonCreator
    public UserDTO(@JsonProperty("userId") Long userId,
                   @JsonProperty("nickname") String nickname,
                   @JsonProperty("profileImage") String profileImage) {
        super(userId, nickname, profileImage);
    }
}