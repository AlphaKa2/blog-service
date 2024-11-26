package com.alphaka.blogservice.exception.custom;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 각 비즈니스 로직의 에러 코드와 메시지 정의
 * 상태 코드, 에러 코드, 메시지를 포함
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 인증 및 인가 관련
    UNAUTHORIZED(401, "AUT001", "인증되지 않은 사용자입니다."),
    SIGNIN_REQUIRED(401, "AUT002", "로그인이 필요합니다."),

    // 사용자 관련
    USER_NOT_FOUND(404, "USR001", "존재하지 않는 사용자입니다."),

    // 블로그 관련
    BLOG_NOT_FOUND(404, "BLG002", "존재하지 않는 블로그입니다."),
    BLOG_CREATION_FAILED(500, "BLG001", "블로그 생성 중 오류가 발생했습니다."),

    // 게시글 관련
    POST_NOT_FOUND(404, "PST001", "존재하지 않는 포스트입니다."),

    // 댓글 관련
    COMMENT_NOT_FOUND(404, "CMT001", "존재하지 않는 댓글입니다."),
    PARENT_COMMENT_NOT_FOUND(404, "CMT002", "존재하지 않는 부모 댓글입니다."),
    INVALID_PARENT_COMMENT(400, "CMT003", "유효하지 않은 부모 댓글입니다."),
    PRIVATE_PARENT_COMMENT(400, "CMT004", "비공개된 부모 댓글입니다."),

    // 신고 관련
    INVALID_REPORT_TARGET(400, "RPT001", "신고 대상이 올바르지 않습니다."),

    // S3 관련
    S3_FILE_EMPTY(400, "S3_001", "파일이 비어있습니다."),
    S3_FILE_EXTENSION_INVALID(400, "S3_002", "지원하지 않는 파일 확장자입니다."),
    S3_FILE_EXTENSION_MISSING(400, "S3_003", "파일 확장자가 없습니다."),
    S3_FILE_SIZE_EXCEEDED(400, "S3_006", "파일 크기가 너무 큽니다."),
    S3_FILE_UPLOAD_FAILED(500, "S3_004", "파일 업로드 중 오류가 발생했습니다."),
    S3_OBJECT_UPLOAD_FAILED(500, "S3_005", "객체 업로드 중 오류가 발생했습니다.");

    private final int status;
    private final String code;
    private final String message;
}
