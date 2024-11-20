package com.alphaka.blogservice.exception.custom;

public class BlogNotFoundException extends CustomException {

    public BlogNotFoundException() {
        super(ErrorCode.BLOG_NOT_FOUND);
    }
}
