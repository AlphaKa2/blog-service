package com.alphaka.blogservice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CommentResponse {

    private Long commentId;
    private Long parentId;
    private Long authorId;
    private String author;
    private String authorProfileImage;
    private String content;
    private List<CommentResponse> children = new ArrayList<>();
    private Long likeCount;
    private boolean isLiked;

    @JsonProperty("isPublic")
    private boolean isPublic;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // QueryDSL을 사용하기 위해 authorProfileImage 필드를 제외 (서비스 로직에서 처리)
    public CommentResponse(Long commentId, Long parentId, Long authorId, String content, Long likeCount,
                           boolean isLiked, boolean isPublic, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.commentId = commentId;
        this.parentId = parentId;
        this.authorId = authorId;
        this.content = content;
        this.likeCount = likeCount;
        this.isLiked = isLiked;
        this.isPublic = isPublic;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
