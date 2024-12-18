package com.alphaka.blogservice.exception.custom;

public class SignInRequiredException extends CustomException {

    public SignInRequiredException() {
        super(ErrorCode.SIGNIN_REQUIRED);
    }
}
