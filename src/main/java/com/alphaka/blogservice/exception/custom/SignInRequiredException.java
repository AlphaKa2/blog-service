package com.alphaka.blogservice.exception.custom;

import com.alphaka.blogservice.exception.CustomException;
import com.alphaka.blogservice.exception.ErrorCode;

public class SignInRequiredException extends CustomException {

    public SignInRequiredException() {
        super(ErrorCode.SIGNIN_REQUIRED);
    }
}
