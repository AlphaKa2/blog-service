package com.alphaka.blogservice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BlogPostListResponse {
    private Long postId;
    private String title;
    private String contentSnippet;
    private String representativeImage;
    private int likeCount;
    private int commentCount;
    private List<String> tags;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private String createdAt;

    @Builder
    public BlogPostListResponse(Long postId, String title, String contentSnippet, String representativeImage, int likeCount, int commentCount, List<String> tags, String createdAt) {
        this.postId = postId;
        this.title = title;
        this.contentSnippet = contentSnippet;
        this.representativeImage = representativeImage;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.tags = tags;
        this.createdAt = createdAt;
    }
}
