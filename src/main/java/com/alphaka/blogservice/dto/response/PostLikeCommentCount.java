package com.alphaka.blogservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostLikeCommentCount {
    private Long postId;
    private Long likeCount;
    private Long commentCount;
}
