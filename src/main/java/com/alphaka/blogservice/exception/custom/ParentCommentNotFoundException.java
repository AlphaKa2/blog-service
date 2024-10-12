package com.alphaka.blogservice.exception.custom;

import com.alphaka.blogservice.exception.CustomException;
import com.alphaka.blogservice.exception.ErrorCode;

public class ParentCommentNotFoundException extends CustomException {

    public ParentCommentNotFoundException() {
        super(ErrorCode.PARENT_COMMENT_NOT_FOUND);
    }
}
