package com.alphaka.blogservice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PostResponse {

    private Long postId;
    private String author;
    private String title;
    private String content;
    private List<String> tags;
    private int likeCount;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Builder
    public PostResponse(Long postId, String author, String title, String content, List<String> tags, int likeCount, LocalDateTime createdAt) {
        this.postId = postId;
        this.author = author;
        this.title = title;
        this.content = content;
        this.tags = tags;
        this.likeCount = likeCount;
        this.createdAt = createdAt;
    }
}