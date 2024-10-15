package com.alphaka.blogservice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 게시글 상세 조회 응답 DTO
 * 게시글 ID, 작성자, 제목, 내용, 태그, 좋아요 수, 조회수, 작성일
 */
@Getter
@Setter
public class PostResponse {

    private Long postId;
    private String author;
    private String title;
    private String content;
    private List<String> tags;
    private int likeCount;
    private int viewCount;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Builder
    public PostResponse(Long postId, String author, String title, String content, List<String> tags,
                        int likeCount, int viewCount, LocalDateTime createdAt) {
        this.postId = postId;
        this.author = author;
        this.title = title;
        this.content = content;
        this.tags = tags;
        this.likeCount = likeCount;
        this.viewCount = viewCount;
        this.createdAt = createdAt;
    }
}