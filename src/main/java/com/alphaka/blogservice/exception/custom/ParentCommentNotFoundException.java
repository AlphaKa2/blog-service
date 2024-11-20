package com.alphaka.blogservice.exception.custom;

public class ParentCommentNotFoundException extends CustomException {

    public ParentCommentNotFoundException() {
        super(ErrorCode.PARENT_COMMENT_NOT_FOUND);
    }
}
