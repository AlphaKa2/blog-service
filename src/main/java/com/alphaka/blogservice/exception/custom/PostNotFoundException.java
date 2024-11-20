package com.alphaka.blogservice.exception.custom;

public class PostNotFoundException extends CustomException {

    public PostNotFoundException() {
        super(ErrorCode.POST_NOT_FOUND);
    }
}
