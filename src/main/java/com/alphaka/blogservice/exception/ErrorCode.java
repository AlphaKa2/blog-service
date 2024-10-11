package com.alphaka.blogservice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // User
    USER_NOT_FOUND(404, "USR001", "존재하지 않는 사용자입니다."),

   // Blog
    BLOG_CREATION_FAILED(500, "BLG001", "블로그 생성 중 오류가 발생했습니다."),
    BLOG_NOT_FOUND(404, "BLG002", "존재하지 않는 블로그입니다."),

    // S3
    S3_FILE_EMPTY(400, "S3_001", "파일이 비어있습니다."),
    S3_FILE_EXTENSION_INVALID(400, "S3_002", "지원하지 않는 파일 확장자입니다."),
    S3_FILE_EXTENSION_MISSING(400, "S3_003", "파일 확장자가 없습니다."),
    S3_FILE_UPLOAD_FAILED(500, "S3_004", "파일 업로드 중 오류가 발생했습니다."),
    S3_OBJECT_UPLOAD_FAILED(500, "S3_005", "객체 업로드 중 오류가 발생했습니다.");



    private final int status;
    private final String code;
    private final String message;
}
