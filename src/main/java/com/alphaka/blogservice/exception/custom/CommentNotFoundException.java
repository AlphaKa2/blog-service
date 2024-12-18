package com.alphaka.blogservice.exception.custom;

public class CommentNotFoundException extends CustomException {

    public CommentNotFoundException() {
        super(ErrorCode.COMMENT_NOT_FOUND);
    }
}
