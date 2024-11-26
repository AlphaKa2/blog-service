package com.alphaka.blogservice.exception.custom;

public class InvalidParentCommentException extends CustomException {

    public InvalidParentCommentException() {
        super(ErrorCode.INVALID_PARENT_COMMENT);
    }
}
