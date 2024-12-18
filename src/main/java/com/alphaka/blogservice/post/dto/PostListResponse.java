package com.alphaka.blogservice.post.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PostListResponse {
    private Long postId;
    private String title;
    private String contentSnippet;
    private String representativeImage;
    private List<String> tags;
    private Long likeCount;
    private Long commentCount;
    private Integer viewCount;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // QueryDSL을 사용하기 위해 썸네일, 게시글 요약, 태그 목록은 제외 (서비스 로직에서 추가)
    public PostListResponse(Long postId, String title, String contentSnippet, Long likeCount, Long commentCount,
                            Integer viewCount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.postId = postId;
        this.title = title;
        this.contentSnippet = contentSnippet;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.viewCount = viewCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
