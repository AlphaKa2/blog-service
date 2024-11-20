package com.alphaka.blogservice.post.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 게시글 상세 조회 응답 DTO
 * 게시글 ID, 작성자, 제목, 내용, 태그, 좋아요 수, 조회수, 작성일
 */
@Getter
@Setter
@NoArgsConstructor
public class PostResponse {

    private Long postId;
    private Long authorId;
    private String author;
    private String title;
    private String content;
    private List<String> tags;
    private Long likeCount;
    private Integer viewCount;
    private boolean isLiked;

    @JsonProperty("isPublic")
    private boolean isPublic;

    @JsonProperty("isCommentable")
    private boolean isCommentable;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // QueryDSL 프로젝션을 위해 author와 tags를 제외 (서비스 로직에서 별도로 추가)
    public PostResponse(Long postId, Long authorId, String title, String content, Long likeCount, Integer viewCount,
                        boolean isLike, boolean isPublic, boolean isCommentable, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.postId = postId;
        this.authorId = authorId;
        this.title = title;
        this.content = content;
        this.likeCount = likeCount;
        this.viewCount = viewCount;
        this.isLiked = isLike;
        this.isPublic = isPublic;
        this.isCommentable = isCommentable;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}