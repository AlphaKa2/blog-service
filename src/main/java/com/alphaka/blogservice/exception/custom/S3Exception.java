package com.alphaka.blogservice.exception.custom;

public class S3Exception extends CustomException {

    private final ErrorCode errorCode;

    public S3Exception(ErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
}
