package com.alphaka.blogservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserInfo {
    private Long userId;
    private String nickname;
    private String profileImage;
}
