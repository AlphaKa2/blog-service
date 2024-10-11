package com.alphaka.blogservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {

    private Long blogId;
    private Long userId;
    private String title;
    private String content;
    private boolean isPublic;
    private boolean isCommentable;
    private int viewCount;
    private String createdAt;
    private String updatedAt;
}