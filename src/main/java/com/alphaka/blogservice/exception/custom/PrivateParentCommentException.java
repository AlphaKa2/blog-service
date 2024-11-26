package com.alphaka.blogservice.exception.custom;

public class PrivateParentCommentException extends CustomException {

    public PrivateParentCommentException() {
        super(ErrorCode.PRIVATE_PARENT_COMMENT);
    }
}
