package com.alphaka.blogservice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PostListResponse {
    private Long postId;
    private String title;
    private String contentSnippet;
    private String representativeImage;
    private int likeCount;
    private int commentCount;
    private int viewCount;
    private List<String> tags;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private String createdAt;

    @Builder
    public PostListResponse(Long postId, String title, String contentSnippet, String representativeImage,
                            int likeCount, int commentCount, int viewCount, List<String> tags, String createdAt) {
        this.postId = postId;
        this.title = title;
        this.contentSnippet = contentSnippet;
        this.representativeImage = representativeImage;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.viewCount = viewCount;
        this.tags = tags;
        this.createdAt = createdAt;
    }
}
