package com.alphaka.blogservice.exception.custom;

public class UnauthorizedException extends CustomException {

    public UnauthorizedException() {
        super(ErrorCode.UNAUTHORIZED);
    }
}
