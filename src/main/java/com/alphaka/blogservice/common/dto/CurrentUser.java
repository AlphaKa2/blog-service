package com.alphaka.blogservice.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUser {
    private Long userId;
    private String nickname;
    private String profileImage;
    private String role;
}
