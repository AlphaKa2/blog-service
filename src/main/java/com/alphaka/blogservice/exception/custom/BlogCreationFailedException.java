package com.alphaka.blogservice.exception.custom;

public class BlogCreationFailedException extends CustomException {

    public BlogCreationFailedException() {
        super(ErrorCode.BLOG_CREATION_FAILED);
    }
}
