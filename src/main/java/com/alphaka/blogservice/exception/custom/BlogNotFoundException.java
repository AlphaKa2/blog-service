package com.alphaka.blogservice.exception.custom;

import com.alphaka.blogservice.exception.CustomException;
import com.alphaka.blogservice.exception.ErrorCode;

public class BlogNotFoundException extends CustomException {

    public BlogNotFoundException() {
        super(ErrorCode.BLOG_NOT_FOUND);
    }
}
