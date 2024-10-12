package com.alphaka.blogservice.exception.custom;

import com.alphaka.blogservice.exception.CustomException;
import com.alphaka.blogservice.exception.ErrorCode;

public class CommentNotFoundException extends CustomException {

    public CommentNotFoundException() {
        super(ErrorCode.COMMENT_NOT_FOUND);
    }
}
