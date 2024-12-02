package com.alphaka.blogservice.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * 사용자 정보를 나타내는 추상 클래스
 */
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@NoArgsConstructor
public abstract class AbstractUser {
    /** 사용자 ID */
    private Long userId;

    /** 사용자 닉네임 */
    private String nickname;

    /** 사용자 프로필 이미지 */
    private String profileImage;
}
