package com.alphaka.blogservice.exception.custom;

import com.alphaka.blogservice.exception.CustomException;
import com.alphaka.blogservice.exception.ErrorCode;

public class BlogCreationFailedException extends CustomException {

    public BlogCreationFailedException() {
        super(ErrorCode.BLOG_CREATION_FAILED);
    }
}
