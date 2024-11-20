package com.alphaka.blogservice.common.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 사용자 정보를 나타내는 추상 클래스
 */
@Getter
@RequiredArgsConstructor
public abstract class AbstractUser {
    /** 사용자 ID */
    protected final Long userId;

    /** 사용자 닉네임 */
    protected final String nickname;

    /** 사용자 프로필 이미지 */
    protected final String profileImage;
}
