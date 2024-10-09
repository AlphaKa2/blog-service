package com.alphaka.blogservice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // User
    USER_NOT_FOUND(404, "USR001", "존재하지 않는 사용자입니다."),

    // Blog
    BLOG_CREATION_FAILED(500, "BLG001", "블로그 생성 중 오류가 발생했습니다.");


    private final int status;
    private final String code;
    private final String message;
}
